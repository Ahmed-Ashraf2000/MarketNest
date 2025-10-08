package com.marketnest.ecommerce.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    public String buildVerificationEmailBody(String username, String verificationUrl) {
        return "Hello " + username + ",\n\n" +
               "Thank you for registering with the Market Nest. To complete your registration, please verify your email by clicking the link below:\n\n" +
               verificationUrl + "\n\n" +
               "This link will expire in 24 hours.\n\n" +
               "If you did not create an account, please ignore this email.\n\n" +
               "Regards,\nBank Security System Team";
    }

    public String buildPasswordResetEmailBody(String username, String resetUrl) {
        return "Hello " + username + ",\n\n" +
               "You recently requested to reset your password. Click the link below to reset it:\n\n" +
               resetUrl + "\n\n" +
               "This link will expire in 10 minutes.\n\n" +
               "If you did not request a password reset, please ignore this email.\n\n" +
               "Regards,\nBank Security System Team";
    }

    public String buildSecurityAlertEmailBody(String username, String timestamp,
                                              String ipAddress, String location,
                                              String deviceInfo) {
        return String.format(
                "<div style='font-family: Arial, sans-serif;'>" +
                "<h2>Security Alert</h2>" +
                "<p>Dear %s,</p>" +
                "<p>We detected a login to your account from a new location or device:</p>" +
                "<ul>" +
                "<li><strong>Time:</strong> %s</li>" +
                "<li><strong>IP Address:</strong> %s</li>" +
                "<li><strong>Location:</strong> %s</li>" +
                "<li><strong>Device:</strong> %s</li>" +
                "</ul>" +
                "<p>If this was you, you can ignore this message. If you didn't log in at this time, " +
                "please change your password immediately and contact our support team.</p>" +
                "<p>Security Team</p>" +
                "</div>",
                username, timestamp, ipAddress, location, deviceInfo);
    }

    public String buildBruteForceAlertEmailBody(String username, int attempts, String ipAddress) {
        return String.format(
                "<div style='font-family: Arial, sans-serif;'>" +
                "<h2>Security Alert - Multiple Failed Login Attempts</h2>" +
                "<p>Dear %s,</p>" +
                "<p>We've detected %d failed login attempts to your account in the past hour from IP %s.</p>" +
                "<p>If this wasn't you, someone may be trying to access your account. " +
                "We recommend changing your password immediately.</p>" +
                "<p>Security Team</p>" +
                "</div>",
                username, attempts, ipAddress);
    }

    public String buildSuccessfulLoginEmailBody(String username, String timestamp,
                                                String ipAddress, String location,
                                                String deviceInfo) {
        return String.format(
                "<div style='font-family: Arial, sans-serif;'>" +
                "<h2>Successful Login Notification</h2>" +
                "<p>Dear %s,</p>" +
                "<p>We detected a successful login to your account:</p>" +
                "<ul>" +
                "<li><strong>Time:</strong> %s</li>" +
                "<li><strong>IP Address:</strong> %s</li>" +
                "<li><strong>Location:</strong> %s</li>" +
                "<li><strong>Device:</strong> %s</li>" +
                "</ul>" +
                "<p>If this was you, no action is needed. If you didn't log in at this time, " +
                "please change your password immediately and contact our support team.</p>" +
                "<p>Security Team</p>" +
                "</div>",
                username, timestamp, ipAddress, location, deviceInfo);
    }

}