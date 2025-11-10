package com.marketnest.ecommerce.service.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplateServiceTest {

    private EmailTemplateService emailTemplateService;

    @BeforeEach
    void setUp() {
        emailTemplateService = new EmailTemplateService();
    }

    @Test
    void buildVerificationEmailBody_shouldContainUsername() {
        String username = "Ahmed Ashraf";
        String verificationUrl = "http://localhost:8080/verify?token=abc123";

        String result = emailTemplateService.buildVerificationEmailBody(username, verificationUrl);

        assertThat(result).contains("Hello Ahmed Ashraf");
    }

    @Test
    void buildVerificationEmailBody_shouldContainVerificationUrl() {
        String username = "Jane Smith";
        String verificationUrl = "http://localhost:8080/verify?token=xyz789";

        String result = emailTemplateService.buildVerificationEmailBody(username, verificationUrl);

        assertThat(result).contains(verificationUrl);
    }

    @Test
    void buildVerificationEmailBody_shouldContainRequiredElements() {
        String username = "Test User";
        String verificationUrl = "http://example.com/verify";

        String result = emailTemplateService.buildVerificationEmailBody(username, verificationUrl);

        assertThat(result)
                .contains("Thank you for registering")
                .contains("Market Nest")
                .contains("expire in 24 hours")
                .contains("Regards");
    }

    @Test
    void buildVerificationEmailBody_shouldHandleSpecialCharactersInUsername() {
        String username = "José García";
        String verificationUrl = "http://example.com/verify";

        String result = emailTemplateService.buildVerificationEmailBody(username, verificationUrl);

        assertThat(result).contains("Hello José García");
    }

    @Test
    void buildPasswordResetEmailBody_shouldContainUsername() {
        String username = "Ahmed Ashraf";
        String resetUrl = "http://localhost:8080/reset?token=reset123";

        String result = emailTemplateService.buildPasswordResetEmailBody(username, resetUrl);

        assertThat(result).contains("Hello Ahmed Ashraf");
    }

    @Test
    void buildPasswordResetEmailBody_shouldContainResetUrl() {
        String username = "Mohamed Hany";
        String resetUrl = "http://localhost:8080/reset?token=reset456";

        String result = emailTemplateService.buildPasswordResetEmailBody(username, resetUrl);

        assertThat(result).contains(resetUrl);
    }

    @Test
    void buildPasswordResetEmailBody_shouldContainRequiredElements() {
        String username = "Test User";
        String resetUrl = "http://example.com/reset";

        String result = emailTemplateService.buildPasswordResetEmailBody(username, resetUrl);

        assertThat(result)
                .contains("reset your password")
                .contains("expire in 10 minutes")
                .contains("If you did not request")
                .contains("Market Nest");
    }

    @Test
    void buildSecurityAlertEmailBody_shouldContainAllParameters() {
        String username = "Ahmed Ashraf";
        String timestamp = "2025-01-15 10:30:00";
        String ipAddress = "192.168.1.1";
        String location = "Cairo, EGY";
        String deviceInfo = "Chrome/Linux";

        String result = emailTemplateService.buildSecurityAlertEmailBody(
                username, timestamp, ipAddress, location, deviceInfo);

        assertThat(result)
                .contains("Dear Ahmed Ashraf")
                .contains(timestamp)
                .contains(ipAddress)
                .contains(location)
                .contains(deviceInfo)
                .contains("Security Alert");
    }

    @Test
    void buildSecurityAlertEmailBody_shouldBeHtmlFormat() {
        String username = "Test User";
        String timestamp = "2025-01-15 10:30:00";
        String ipAddress = "10.0.0.1";
        String location = "London, UK";
        String deviceInfo = "Safari/MacOS";

        String result = emailTemplateService.buildSecurityAlertEmailBody(
                username, timestamp, ipAddress, location, deviceInfo);

        assertThat(result)
                .contains("<div")
                .contains("<h2>")
                .contains("<p>")
                .contains("<ul>")
                .contains("<li>")
                .contains("<strong>");
    }

    @Test
    void buildSecurityAlertEmailBody_shouldContainSecurityInstructions() {
        String result = emailTemplateService.buildSecurityAlertEmailBody(
                "User", "timestamp", "ip", "location", "device");

        assertThat(result)
                .contains("new location or device")
                .contains("change your password")
                .contains("contact our support team");
    }

    @Test
    void buildBruteForceAlertEmailBody_shouldContainUsername() {
        String username = "Ahmed Ashraf";
        int attempts = 5;
        String ipAddress = "203.0.113.1";

        String result =
                emailTemplateService.buildBruteForceAlertEmailBody(username, attempts, ipAddress);

        assertThat(result).contains("Dear Ahmed Ashraf");
    }

    @Test
    void buildBruteForceAlertEmailBody_shouldContainAttemptCount() {
        String username = "Mohamed Hany";
        int attempts = 7;
        String ipAddress = "198.51.100.1";

        String result =
                emailTemplateService.buildBruteForceAlertEmailBody(username, attempts, ipAddress);

        assertThat(result).contains("7 failed login attempts");
    }

    @Test
    void buildBruteForceAlertEmailBody_shouldContainIpAddress() {
        String username = "Test User";
        int attempts = 10;
        String ipAddress = "192.0.2.1";

        String result =
                emailTemplateService.buildBruteForceAlertEmailBody(username, attempts, ipAddress);

        assertThat(result).contains(ipAddress);
    }

    @Test
    void buildBruteForceAlertEmailBody_shouldBeHtmlFormat() {
        String result = emailTemplateService.buildBruteForceAlertEmailBody("User", 5, "1.2.3.4");

        assertThat(result)
                .contains("<div")
                .contains("<h2>")
                .contains("<p>")
                .contains("Multiple Failed Login Attempts");
    }

    @Test
    void buildBruteForceAlertEmailBody_shouldContainSecurityWarning() {
        String result = emailTemplateService.buildBruteForceAlertEmailBody("User", 5, "1.2.3.4");

        assertThat(result)
                .contains("someone may be trying to access your account")
                .contains("changing your password")
                .contains("Security Team");
    }

    @Test
    void buildSuccessfulLoginEmailBody_shouldContainAllParameters() {
        String username = "Ahmed Ashraf";
        String timestamp = "2025-01-15 14:20:00";
        String ipAddress = "10.20.30.40";
        String location = "San Francisco, USA";
        String deviceInfo = "Firefox/Windows";

        String result = emailTemplateService.buildSuccessfulLoginEmailBody(
                username, timestamp, ipAddress, location, deviceInfo);

        assertThat(result)
                .contains("Dear Ahmed Ashraf")
                .contains(timestamp)
                .contains(ipAddress)
                .contains(location)
                .contains(deviceInfo);
    }

    @Test
    void buildSuccessfulLoginEmailBody_shouldBeHtmlFormat() {
        String result = emailTemplateService.buildSuccessfulLoginEmailBody(
                "User", "time", "ip", "location", "device");

        assertThat(result)
                .contains("<div")
                .contains("<h2>")
                .contains("<ul>")
                .contains("<li>")
                .contains("Successful Login Notification");
    }

    @Test
    void buildSuccessfulLoginEmailBody_shouldContainLoginDetails() {
        String result = emailTemplateService.buildSuccessfulLoginEmailBody(
                "User", "time", "ip", "location", "device");

        assertThat(result)
                .contains("successful login to your account")
                .contains("<strong>Time:</strong>")
                .contains("<strong>IP Address:</strong>")
                .contains("<strong>Location:</strong>")
                .contains("<strong>Device:</strong>");
    }

    @Test
    void buildSuccessfulLoginEmailBody_shouldContainSecurityInstructions() {
        String result = emailTemplateService.buildSuccessfulLoginEmailBody(
                "User", "time", "ip", "location", "device");

        assertThat(result)
                .contains("If this was you, no action is needed")
                .contains("change your password immediately")
                .contains("Security Team");
    }

    @Test
    void allEmailTemplates_shouldHandleNullValues() {
        String result1 = emailTemplateService.buildVerificationEmailBody(null, null);
        String result2 = emailTemplateService.buildPasswordResetEmailBody(null, null);
        String result3 =
                emailTemplateService.buildSecurityAlertEmailBody(null, null, null, null, null);
        String result4 = emailTemplateService.buildBruteForceAlertEmailBody(null, 0, null);
        String result5 =
                emailTemplateService.buildSuccessfulLoginEmailBody(null, null, null, null, null);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result3).isNotNull();
        assertThat(result4).isNotNull();
        assertThat(result5).isNotNull();
    }

    @Test
    void allEmailTemplates_shouldHandleEmptyStrings() {
        String result1 = emailTemplateService.buildVerificationEmailBody("", "");
        String result2 = emailTemplateService.buildPasswordResetEmailBody("", "");
        String result3 = emailTemplateService.buildSecurityAlertEmailBody("", "", "", "", "");
        String result4 = emailTemplateService.buildBruteForceAlertEmailBody("", 0, "");
        String result5 = emailTemplateService.buildSuccessfulLoginEmailBody("", "", "", "", "");

        assertThat(result1).isNotEmpty();
        assertThat(result2).isNotEmpty();
        assertThat(result3).isNotEmpty();
        assertThat(result4).isNotEmpty();
        assertThat(result5).isNotEmpty();
    }

    @Test
    void buildBruteForceAlertEmailBody_shouldHandleLargeAttemptCount() {
        String result = emailTemplateService.buildBruteForceAlertEmailBody("User", 9999, "1.2.3.4");

        assertThat(result).contains("9999 failed login attempts");
    }

    @Test
    void buildVerificationEmailBody_shouldHandleLongUrls() {
        String longUrl = "http://localhost:8080/verify?token=" + "a".repeat(500);
        String result = emailTemplateService.buildVerificationEmailBody("User", longUrl);

        assertThat(result).contains(longUrl);
    }

    @Test
    void buildPasswordResetEmailBody_shouldHandleLongUrls() {
        String longUrl = "http://localhost:8080/reset?token=" + "b".repeat(500);
        String result = emailTemplateService.buildPasswordResetEmailBody("User", longUrl);

        assertThat(result).contains(longUrl);
    }
}