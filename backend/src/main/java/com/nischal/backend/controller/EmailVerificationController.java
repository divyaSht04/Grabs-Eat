package com.nischal.backend.controller;

import com.nischal.backend.dto.request.ForgotPasswordRequest;
import com.nischal.backend.dto.request.ResetPasswordRequest;
import com.nischal.backend.dto.request.VerifyEmailRequest;
import com.nischal.backend.entity.User;
import com.nischal.backend.service.AuthService;
import com.nischal.backend.service.userdetails.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final AuthService authService;

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        authService.verifyEmail(userDetails.getUser(), request.getCode());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email verified successfully"
        ));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authService.resendVerificationCode(userDetails.getUser());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Verification code sent to your email"
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        authService.sendPasswordResetCode(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password reset code sent to your email"
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            @RequestParam String email
    ) {
        authService.resetPassword(email, request.getCode(), request.getNewPassword());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password reset successfully"
        ));
    }
}
