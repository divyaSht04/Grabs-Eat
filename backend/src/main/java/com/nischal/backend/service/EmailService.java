package com.nischal.backend.service;

import com.nischal.backend.entity.EmailVerification;

public interface EmailService {

    void sendVerificationEmail(String toEmail, String fullName, String verificationCode);

    void sendPasswordResetEmail(String toEmail, String fullName, String verificationCode);
}
