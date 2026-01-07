package com.nischal.backend.jwt;

import com.nischal.backend.entity.User;
import com.nischal.backend.service.TokenBlacklistService;
import com.nischal.backend.service.userdetails.CustomUserDetails;
import com.nischal.backend.service.userdetails.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        final String requestPath = request.getRequestURI();

        // Allow verification endpoints to bypass email verification check
        boolean isVerificationEndpoint = requestPath.equals("/api/v1/auth/verify-email")
                || requestPath.equals("/api/v1/auth/resend-verification");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);

        try {
            if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                logger.warn("Attempted to use blacklisted token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Token has been revoked\"}");
                response.setContentType("application/json");
                return;
            }

            userEmail = jwtUtil.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // Check if email is verified (skip for verification endpoints)
                if (!isVerificationEndpoint && userDetails instanceof CustomUserDetails customUserDetails) {
                    User user = customUserDetails.getUser();
                    if (!user.getIsEmailVerified()) {
                        logger.warn("User attempted to access API without email verification: " + userEmail);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("{\"error\":\"Email not verified. Please verify your email to access this resource.\"}");
                        response.setContentType("application/json");
                        return;
                    }
                }

                // Validate token
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            logger.error("JWT authentication failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
