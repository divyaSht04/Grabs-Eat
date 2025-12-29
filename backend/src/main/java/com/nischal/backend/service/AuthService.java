package com.nischal.backend.service;

import com.nischal.backend.dto.auth.AuthResponse;
import com.nischal.backend.dto.auth.LoginRequest;
import com.nischal.backend.dto.auth.RefreshTokenRequest;
import com.nischal.backend.dto.auth.RegisterRequest;
import com.nischal.backend.entity.User;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(String accessToken);

    boolean existsByEmail(String email);

    void verifyEmail(String email, String code);

    void resendVerificationCode(String email);

    void sendPasswordResetCode(String email);

    void resetPassword(String email, String code, String newPassword);
}
