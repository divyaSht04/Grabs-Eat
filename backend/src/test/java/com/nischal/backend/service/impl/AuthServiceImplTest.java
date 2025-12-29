package com.nischal.backend.service.impl;

import com.nischal.backend.dto.auth.*;
import com.nischal.backend.entity.RefreshToken;
import com.nischal.backend.entity.Role;
import com.nischal.backend.entity.User;
import com.nischal.backend.exception.BadRequestException;
import com.nischal.backend.exception.UnauthorizedException;
import com.nischal.backend.jwt.JwtUtil;
import com.nischal.backend.mapper.UserMapper;
import com.nischal.backend.service.RefreshTokenService;
import com.nischal.backend.service.TokenBlacklistService;
import com.nischal.backend.service.UserService;
import com.nischal.backend.service.userdetails.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private UserResponse testUserResponse;
    private CustomUserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@example.com")
                .password("encodedPassword123")
                .phoneNumber("9841234567")
                .role(Role.CUSTOMER)
                .isActive(true)
                .isEmailVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUserResponse = UserResponse.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@example.com")
                .phoneNumber("9841234567")
                .role(Role.CUSTOMER)
                .isActive(true)
                .isEmailVerified(false)
                .build();

        testUserDetails = new CustomUserDetails(testUser);
    }

    // ==================== REGISTER TESTS ====================

    @Test
    @DisplayName("Should register user successfully")
    void register_Success() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .fullName("New User")
                .email("new@example.com")
                .password("Password123")
                .phoneNumber("9841234568")
                .role(Role.CUSTOMER)
                .build();

        User newUser = User.builder()
                .email("new@example.com")
                .fullName("New User")
                .role(Role.CUSTOMER)
                .build();

        User savedUser = User.builder()
                .id(2L)
                .email("new@example.com")
                .fullName("New User")
                .password("encodedPassword")
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id(2L)
                .email("new@example.com")
                .fullName("New User")
                .role(Role.CUSTOMER)
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .token("refresh-token-uuid")
                .user(savedUser)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .build();

        when(userService.existsByEmail("new@example.com")).thenReturn(false);
        when(userService.existsByPhoneNumber("9841234568")).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(newUser);
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
        when(userService.createUser(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateAccessToken(any(CustomUserDetails.class))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(savedUser)).thenReturn(refreshToken);
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(86400L);
        when(userMapper.toResponse(savedUser)).thenReturn(userResponse);

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-uuid");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(86400L);
        assertThat(response.getUser().getEmail()).isEqualTo("new@example.com");

        verify(userService, times(1)).existsByEmail("new@example.com");
        verify(userService, times(1)).existsByPhoneNumber("9841234568");
        verify(passwordEncoder, times(1)).encode("Password123");
        verify(userService, times(1)).createUser(any(User.class));
        verify(jwtUtil, times(1)).generateAccessToken(any(CustomUserDetails.class));
        verify(refreshTokenService, times(1)).createRefreshToken(savedUser);
    }

    @Test
    @DisplayName("Should throw BadRequestException when email already exists during registration")
    void register_EmailAlreadyExists() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password123")
                .build();

        when(userService.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email already exists");

        verify(userService, times(1)).existsByEmail("test@example.com");
        verify(userService, never()).createUser(any());
    }

    @Test
    @DisplayName("Should throw BadRequestException when phone number already exists during registration")
    void register_PhoneNumberAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .email("new@example.com")
                .phoneNumber("9841234567")
                .password("Password123")
                .build();

        when(userService.existsByEmail("new@example.com")).thenReturn(false);
        when(userService.existsByPhoneNumber("9841234567")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Phone number already exists");

        verify(userService, times(1)).existsByEmail("new@example.com");
        verify(userService, times(1)).existsByPhoneNumber("9841234567");
        verify(userService, never()).createUser(any());
    }

    @Test
    @DisplayName("Should register successfully without phone number")
    void register_WithoutPhoneNumber() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .fullName("No Phone User")
                .email("nophone@example.com")
                .password("Password123")
                .phoneNumber(null)
                .role(Role.CUSTOMER)
                .build();

        User newUser = User.builder()
                .email("nophone@example.com")
                .fullName("No Phone User")
                .role(Role.CUSTOMER)
                .build();

        User savedUser = User.builder()
                .id(3L)
                .email("nophone@example.com")
                .fullName("No Phone User")
                .password("encodedPassword")
                .role(Role.CUSTOMER)
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id(3L)
                .email("nophone@example.com")
                .fullName("No Phone User")
                .role(Role.CUSTOMER)
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .token("refresh-token-uuid")
                .user(savedUser)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .build();

        when(userService.existsByEmail("nophone@example.com")).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(newUser);
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
        when(userService.createUser(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateAccessToken(any(CustomUserDetails.class))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(savedUser)).thenReturn(refreshToken);
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(86400L);
        when(userMapper.toResponse(savedUser)).thenReturn(userResponse);

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        verify(userService, never()).existsByPhoneNumber(anyString());
    }

    @Test
    @DisplayName("Should register successfully with empty phone number")
    void register_WithEmptyPhoneNumber() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Empty Phone User")
                .email("emptyphone@example.com")
                .password("Password123")
                .phoneNumber("")
                .role(Role.CUSTOMER)
                .build();

        User newUser = User.builder()
                .email("emptyphone@example.com")
                .fullName("Empty Phone User")
                .role(Role.CUSTOMER)
                .build();

        User savedUser = User.builder()
                .id(4L)
                .email("emptyphone@example.com")
                .fullName("Empty Phone User")
                .password("encodedPassword")
                .role(Role.CUSTOMER)
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id(4L)
                .email("emptyphone@example.com")
                .fullName("Empty Phone User")
                .role(Role.CUSTOMER)
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(2L)
                .token("refresh-token-uuid-2")
                .user(savedUser)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .build();

        when(userService.existsByEmail("emptyphone@example.com")).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(newUser);
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
        when(userService.createUser(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateAccessToken(any(CustomUserDetails.class))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(savedUser)).thenReturn(refreshToken);
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(86400L);
        when(userMapper.toResponse(savedUser)).thenReturn(userResponse);

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        verify(userService, never()).existsByPhoneNumber(anyString());
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("Should login successfully")
    void login_Success() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("Password123")
                .build();

        Authentication authentication = mock(Authentication.class);

        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .token("refresh-token-uuid")
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUserDetails);
        when(refreshTokenService.createRefreshToken(testUser)).thenReturn(refreshToken);
        when(jwtUtil.generateAccessToken(testUserDetails)).thenReturn("access-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(86400L);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-uuid");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(86400L);
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(refreshTokenService, times(1)).revokeAllUserTokens(testUser);
        verify(jwtUtil, times(1)).generateAccessToken(testUserDetails);
        verify(refreshTokenService, times(1)).createRefreshToken(testUser);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when credentials are invalid")
    void login_InvalidCredentials() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("WrongPassword")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when user not found")
    void login_UserNotFound() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("notfound@example.com")
                .password("Password123")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("User not found"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    // ==================== REFRESH TOKEN TESTS ====================

    @Test
    @DisplayName("Should refresh token successfully")
    void refreshToken_Success() {
        // Arrange
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("valid-refresh-token-uuid")
                .build();

        RefreshToken oldRefreshToken = RefreshToken.builder()
                .id(5L)
                .token("valid-refresh-token-uuid")
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .build();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .id(6L)
                .token("new-refresh-token-uuid")
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .build();

        when(refreshTokenService.validateRefreshToken("valid-refresh-token-uuid")).thenReturn(oldRefreshToken);
        when(refreshTokenService.createRefreshToken(testUser)).thenReturn(newRefreshToken);
        when(jwtUtil.generateAccessToken(any(CustomUserDetails.class))).thenReturn("new-access-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(86400L);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // Act
        AuthResponse response = authService.refreshToken(request.getRefreshToken());

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token-uuid");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");

        verify(refreshTokenService, times(1)).validateRefreshToken("valid-refresh-token-uuid");
        verify(refreshTokenService, times(1)).revokeRefreshToken("valid-refresh-token-uuid");
        verify(refreshTokenService, times(1)).createRefreshToken(testUser);
        verify(jwtUtil, times(1)).generateAccessToken(any(CustomUserDetails.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when refresh token is invalid")
    void refreshToken_InvalidToken() {
        // Arrange
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("invalid-token")
                .build();

        when(refreshTokenService.validateRefreshToken("invalid-token"))
                .thenThrow(new UnauthorizedException("Invalid or expired refresh token"));

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(request.getRefreshToken()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid or expired refresh token");

        verify(refreshTokenService, times(1)).validateRefreshToken("invalid-token");
        verify(refreshTokenService, never()).revokeRefreshToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when refresh token is expired")
    void refreshToken_ExpiredToken() {
        // Arrange
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("expired-token")
                .build();

        when(refreshTokenService.validateRefreshToken("expired-token"))
                .thenThrow(new UnauthorizedException("Invalid or expired refresh token"));

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(request.getRefreshToken()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid or expired refresh token");

        verify(refreshTokenService, times(1)).validateRefreshToken("expired-token");
    }

    // ==================== LOGOUT TESTS ====================

    @Test
    @DisplayName("Should logout successfully (no-op)")
    void logout_Success() {
        // Arrange
        String token = "valid-token";

        // Act
        authService.logout(token);

        // Assert
        // Logout is currently a no-op, so just verify no exceptions are thrown
        // In future, this could verify token blacklisting
        verifyNoInteractions(userService);
        verifyNoInteractions(jwtUtil);
        verifyNoInteractions(authenticationManager);
    }

    // ==================== INTEGRATION SCENARIO TESTS ====================

    @Test
    @DisplayName("Integration: Complete registration and login flow")
    void integrationTest_RegisterAndLogin() {
        // Step 1: Register
        RegisterRequest registerRequest = RegisterRequest.builder()
                .fullName("Integration User")
                .email("integration@example.com")
                .password("Password123")
                .phoneNumber("9841234569")
                .role(Role.CUSTOMER)
                .build();

        User newUser = User.builder()
                .email("integration@example.com")
                .fullName("Integration User")
                .role(Role.CUSTOMER)
                .build();

        User savedUser = User.builder()
                .id(5L)
                .email("integration@example.com")
                .fullName("Integration User")
                .password("encodedPassword")
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id(5L)
                .email("integration@example.com")
                .fullName("Integration User")
                .role(Role.CUSTOMER)
                .build();

        RefreshToken registerRefreshToken = RefreshToken.builder()
                .id(3L)
                .token("register-refresh-token-uuid")
                .user(savedUser)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .build();

        when(userService.existsByEmail("integration@example.com")).thenReturn(false);
        when(userService.existsByPhoneNumber("9841234569")).thenReturn(false);
        when(userMapper.toEntity(registerRequest)).thenReturn(newUser);
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
        when(userService.createUser(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateAccessToken(any(CustomUserDetails.class))).thenReturn("register-access-token");
        when(refreshTokenService.createRefreshToken(savedUser)).thenReturn(registerRefreshToken);
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(86400L);
        when(userMapper.toResponse(savedUser)).thenReturn(userResponse);

        AuthResponse registerResponse = authService.register(registerRequest);

        // Assert registration
        assertThat(registerResponse).isNotNull();
        assertThat(registerResponse.getAccessToken()).isEqualTo("register-access-token");

        // Step 2: Login
        LoginRequest loginRequest = LoginRequest.builder()
                .email("integration@example.com")
                .password("Password123")
                .build();

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails integrationUserDetails = new CustomUserDetails(savedUser);

        RefreshToken loginRefreshToken = RefreshToken.builder()
                .id(4L)
                .token("login-refresh-token-uuid")
                .user(savedUser)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(integrationUserDetails);
        when(refreshTokenService.createRefreshToken(savedUser)).thenReturn(loginRefreshToken);
        when(jwtUtil.generateAccessToken(integrationUserDetails)).thenReturn("login-access-token");

        AuthResponse loginResponse = authService.login(loginRequest);

        // Assert login
        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.getAccessToken()).isEqualTo("login-access-token");
        assertThat(loginResponse.getUser().getEmail()).isEqualTo("integration@example.com");
    }
}
