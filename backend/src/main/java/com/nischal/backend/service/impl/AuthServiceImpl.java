package com.nischal.backend.service.impl;

import com.nischal.backend.dto.auth.AuthResponse;
import com.nischal.backend.dto.auth.LoginRequest;
import com.nischal.backend.dto.auth.RegisterRequest;
import com.nischal.backend.entity.EmailVerification;
import com.nischal.backend.entity.RefreshToken;
import com.nischal.backend.entity.User;
import com.nischal.backend.exception.BadRequestException;
import com.nischal.backend.exception.ResourceNotFoundException;
import com.nischal.backend.exception.UnauthorizedException;
import com.nischal.backend.mapper.UserMapper;
import com.nischal.backend.jwt.JwtUtil;
import com.nischal.backend.repository.UserRepository;
import com.nischal.backend.service.AuthService;
import com.nischal.backend.service.EmailService;
import com.nischal.backend.service.EmailVerificationService;
import com.nischal.backend.service.RefreshTokenService;
import com.nischal.backend.service.TokenBlacklistService;
import com.nischal.backend.service.UserService;
import com.nischal.backend.service.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate email uniqueness
        if (userService.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // Validate phone number uniqueness if provided
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            if (userService.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new BadRequestException("Phone number already exists");
            }
        }

        // Create user entity
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save user
        User savedUser = userService.createUser(user);

        // Send verification email
        EmailVerification verification = emailVerificationService.createVerificationCode(
                savedUser,
                EmailVerification.VerificationType.EMAIL_VERIFICATION
        );
        emailService.sendVerificationEmail(
                savedUser.getEmail(),
                savedUser.getFullName(),
                verification.getVerificationCode()
        );

        // Wrap in CustomUserDetails for proper RBA
        CustomUserDetails userDetails = new CustomUserDetails(savedUser);

        // Generate access token
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        
        // Create refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        log.info("User registered successfully: {}", savedUser.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .user(userMapper.toResponse(savedUser))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Get CustomUserDetails from authentication
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // Check if user is active
            if (!user.getIsActive()) {
                throw new UnauthorizedException("Account is deactivated");
            }

            // Revoke all existing refresh tokens for this user
            refreshTokenService.revokeAllUserTokens(user);

            // Generate new access token
            String accessToken = jwtUtil.generateAccessToken(userDetails);
            
            // Create new refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            log.info("User logged in successfully: {}", user.getEmail());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getAccessTokenExpiration())
                    .user(userMapper.toResponse(user))
                    .build();

        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        // Validate and get refresh token
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenStr);
        User user = refreshToken.getUser();

        // Revoke the old refresh token (Token Rotation)
        refreshTokenService.revokeRefreshToken(refreshTokenStr);

        // Wrap in CustomUserDetails for proper RBA
        CustomUserDetails userDetails = new CustomUserDetails(user);

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(userDetails);
        
        // Create new refresh token
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        log.info("Tokens refreshed for user: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(String accessToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // Blacklist the current access token
            LocalDateTime expiryDate = LocalDateTime.now()
                    .plusSeconds(jwtUtil.getAccessTokenExpiration() / 1000);
            tokenBlacklistService.blacklistToken(accessToken, expiryDate, "LOGOUT", user.getId());

            // Revoke all refresh tokens for this user
            refreshTokenService.revokeAllUserTokens(user);

            SecurityContextHolder.clearContext();

            log.info("User logged out successfully: {}", user.getEmail());
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        return userService.existsByEmail(email);
    }

    @Override
    @Transactional
    public void verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getIsEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        boolean isValid = emailVerificationService.verifyCode(
                user,
                code,
                EmailVerification.VerificationType.EMAIL_VERIFICATION
        );

        if (isValid) {
            user.setIsEmailVerified(true);
            userRepository.save(user);
            log.info("Email verified for user: {}", user.getEmail());
        }
    }

    @Override
    @Transactional
    public void resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getIsEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        EmailVerification verification = emailVerificationService.createVerificationCode(
                user,
                EmailVerification.VerificationType.EMAIL_VERIFICATION
        );

        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFullName(),
                verification.getVerificationCode()
        );

        log.info("Verification code resent to: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void sendPasswordResetCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        EmailVerification verification = emailVerificationService.createVerificationCode(
                user,
                EmailVerification.VerificationType.PASSWORD_RESET
        );

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFullName(),
                verification.getVerificationCode()
        );

        log.info("Password reset code sent to: {}", email);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        boolean isValid = emailVerificationService.verifyCode(
                user,
                code,
                EmailVerification.VerificationType.PASSWORD_RESET
        );

        if (isValid) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Revoke all refresh tokens for security
            refreshTokenService.revokeAllUserTokens(user);

            log.info("Password reset successfully for user: {}", email);
        }
    }
}
