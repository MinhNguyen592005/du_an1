package com.vu.springapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Password Reset OTP - Shop");
            message.setText(buildEmailContent(otp));

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email. Please try again later.");
        }
    }

    private String buildEmailContent(String otp) {
        return String.format(
            "Hello,\n\n" +
            "You have requested to reset your password.\n\n" +
            "Your OTP code is: %s\n\n" +
            "This code will expire in 5 minutes.\n\n" +
            "If you did not request this password reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Shop Team",
            otp
        );
    }
}

