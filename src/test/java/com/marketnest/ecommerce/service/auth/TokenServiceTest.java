package com.marketnest.ecommerce.service.auth;

import com.marketnest.ecommerce.exception.InvalidVerificationToken;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.model.VerificationToken;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.repository.VerificationTokenRepository;
import com.marketnest.ecommerce.service.email.EmailService;
import com.marketnest.ecommerce.service.email.EmailTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailTemplateService emailTemplateService;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @InjectMocks
    private TokenService tokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "RESEND_COOLDOWN_MINUTES", 5L);

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Ahmed");
        testUser.setLastName("Ashraf");
        testUser.setActive(false);
    }

    @Test
    void generateToken_shouldCreateEmailVerificationToken_whenEmailVerificationType() {
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(i -> i.getArgument(0));

        String rawToken = tokenService.generateToken(testUser,
                VerificationToken.TokenType.EMAIL_VERIFICATION);

        assertThat(rawToken).isNotNull();
        assertThat(rawToken).hasSize(64);

        ArgumentCaptor<VerificationToken> captor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(verificationTokenRepository).save(captor.capture());

        VerificationToken saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(testUser);
        assertThat(saved.getTokenType()).isEqualTo(VerificationToken.TokenType.EMAIL_VERIFICATION);
        assertThat(saved.getToken()).isNotNull();
        assertThat(saved.getToken()).isNotEqualTo(rawToken);
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void generateToken_shouldCreatePasswordResetToken_whenPasswordResetType() {
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(i -> i.getArgument(0));

        String rawToken =
                tokenService.generateToken(testUser, VerificationToken.TokenType.PASSWORD_RESET);

        assertThat(rawToken).isNotNull();

        ArgumentCaptor<VerificationToken> captor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(verificationTokenRepository).save(captor.capture());

        VerificationToken saved = captor.getValue();
        assertThat(saved.getTokenType()).isEqualTo(VerificationToken.TokenType.PASSWORD_RESET);
        assertThat(saved.getUser()).isEqualTo(testUser);
    }

    @Test
    void generateToken_shouldGenerateUniqueTokens() {
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(i -> i.getArgument(0));

        String token1 = tokenService.generateToken(testUser,
                VerificationToken.TokenType.EMAIL_VERIFICATION);
        String token2 = tokenService.generateToken(testUser,
                VerificationToken.TokenType.EMAIL_VERIFICATION);

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void sendVerificationEmail_shouldSendEmailWithVerificationLink() {
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(emailTemplateService.buildVerificationEmailBody(anyString(), anyString()))
                .thenReturn("Email body content");

        tokenService.sendVerificationEmail(testUser, "http://localhost:8080");

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailService).sendEmail(
                emailCaptor.capture(),
                subjectCaptor.capture(),
                bodyCaptor.capture()
        );

        assertThat(emailCaptor.getValue()).isEqualTo("test@example.com");
        assertThat(subjectCaptor.getValue()).isEqualTo("Verify Your Email");
        assertThat(bodyCaptor.getValue()).isEqualTo("Email body content");

        verify(emailTemplateService).buildVerificationEmailBody(
                eq("Ahmed Ashraf"),
                contains("http://localhost:8080/api/auth/verify-email?token=")
        );
    }

    @Test
    void sendVerificationEmail_shouldGenerateValidVerificationUrl() {
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(emailTemplateService.buildVerificationEmailBody(anyString(), anyString()))
                .thenReturn("Email body");

        tokenService.sendVerificationEmail(testUser, "http://localhost:8080");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailTemplateService).buildVerificationEmailBody(anyString(), urlCaptor.capture());

        String capturedUrl = urlCaptor.getValue();
        assertThat(capturedUrl).startsWith("http://localhost:8080/api/auth/verify-email?token=");
        assertThat(capturedUrl).hasSize(
                "http://localhost:8080/api/auth/verify-email?token=".length() + 64);
    }

    @Test
    void sendPasswordResetEmail_shouldSendEmailWithResetLink() {
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(emailTemplateService.buildPasswordResetEmailBody(anyString(), anyString()))
                .thenReturn("Reset email body");

        tokenService.sendPasswordResetEmail(testUser, "http://localhost:8080");

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailService).sendEmail(
                emailCaptor.capture(),
                subjectCaptor.capture(),
                bodyCaptor.capture()
        );

        assertThat(emailCaptor.getValue()).isEqualTo("test@example.com");
        assertThat(subjectCaptor.getValue()).contains("Password Reset");
        assertThat(bodyCaptor.getValue()).isEqualTo("Reset email body");

        verify(emailTemplateService).buildPasswordResetEmailBody(
                eq("Ahmed Ashraf"),
                contains("http://localhost:8080/api/auth/reset-password?token=")
        );
    }

    @Test
    void verifyToken_shouldActivateUser_whenValidEmailVerificationToken() {
        String rawToken = "valid-token-123";
        String hashedToken = "hashed-token-123";

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(hashedToken);
        verificationToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        verificationToken.setExpiresAt(LocalDateTime.now().plusSeconds(3600));
        verificationToken.setUser(testUser);

        when(verificationTokenRepository.findFirstByUserAndTokenTypeAndUsedFalseOrderByIssuedAtDesc(
                testUser, eq(VerificationToken.TokenType.EMAIL_VERIFICATION)))
                .thenReturn(Optional.of(verificationToken));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result =
                tokenService.verifyToken(rawToken, VerificationToken.TokenType.EMAIL_VERIFICATION);

        assertThat(result).isNotNull();
        assertThat(result.isActive()).isTrue();

        verify(userRepository).save(testUser);
        verify(verificationTokenRepository).delete(verificationToken);
    }

    @Test
    void verifyToken_shouldThrowException_whenTokenNotFound() {
        when(verificationTokenRepository.findFirstByUserAndTokenTypeAndUsedFalseOrderByIssuedAtDesc(
                testUser, any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tokenService.verifyToken("invalid-token",
                VerificationToken.TokenType.EMAIL_VERIFICATION))
                .isInstanceOf(InvalidVerificationToken.class)
                .hasMessageContaining("Invalid or expired verification token");

        verify(userRepository, never()).save(any());
        verify(verificationTokenRepository, never()).delete(any());
    }

    @Test
    void verifyToken_shouldThrowException_whenTokenExpired() {
        String rawToken = "expired-token";
        String hashedToken = "hashed-expired-token";

        VerificationToken expiredToken = new VerificationToken();
        expiredToken.setToken(hashedToken);
        expiredToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        expiredToken.setExpiresAt(LocalDateTime.now().minusSeconds(3600));
        expiredToken.setUser(testUser);

        when(verificationTokenRepository.findFirstByUserAndTokenTypeAndUsedFalseOrderByIssuedAtDesc(
                testUser, any()))
                .thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> tokenService.verifyToken(rawToken,
                VerificationToken.TokenType.EMAIL_VERIFICATION))
                .isInstanceOf(InvalidVerificationToken.class)
                .hasMessageContaining("Invalid or expired verification token");

        verify(userRepository, never()).save(any());
        verify(verificationTokenRepository).delete(expiredToken);
    }

    @Test
    void verifyToken_shouldReturnUser_whenValidPasswordResetToken() {
        String rawToken = "valid-reset-token";
        String hashedToken = "hashed-reset-token";

        VerificationToken resetToken = new VerificationToken();
        resetToken.setToken(hashedToken);
        resetToken.setTokenType(VerificationToken.TokenType.PASSWORD_RESET);
        resetToken.setExpiresAt(LocalDateTime.now().plusSeconds(3600));
        resetToken.setUser(testUser);

        when(verificationTokenRepository.findFirstByUserAndTokenTypeAndUsedFalseOrderByIssuedAtDesc(
                testUser, eq(VerificationToken.TokenType.PASSWORD_RESET)))
                .thenReturn(Optional.of(resetToken));

        User result =
                tokenService.verifyToken(rawToken, VerificationToken.TokenType.PASSWORD_RESET);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testUser);

        verify(verificationTokenRepository).delete(resetToken);
        verify(userRepository, never()).save(any());
    }

    @Test
    void resendToken_shouldSendNewToken_whenCooldownPeriodPassed() {
        VerificationToken oldToken = new VerificationToken();
        oldToken.setToken("old-token");
        oldToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        oldToken.setExpiresAt(LocalDateTime.now().minusSeconds(600));
        oldToken.setIssuedAt(LocalDateTime.now().minusSeconds(600));
        oldToken.setUser(testUser);

        when(verificationTokenRepository.findFirstByUserAndTokenTypeAndUsedFalseOrderByIssuedAtDesc(
                testUser, VerificationToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(oldToken));
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(emailTemplateService.buildVerificationEmailBody(anyString(), anyString()))
                .thenReturn("Email body");

        tokenService.resendToken(testUser, "http://localhost:8080",
                VerificationToken.TokenType.EMAIL_VERIFICATION);

        verify(verificationTokenRepository).delete(oldToken);
        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendEmail(eq("test@example.com"), anyString(), anyString());
    }

    @Test
    void resendToken_shouldThrowException_whenCooldownNotPassed() {
        VerificationToken recentToken = new VerificationToken();
        recentToken.setToken("recent-token");
        recentToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        recentToken.setExpiresAt(LocalDateTime.now().plusSeconds(3600));
        recentToken.setIssuedAt(LocalDateTime.now().minusSeconds(60));
        recentToken.setUser(testUser);

        when(verificationTokenRepository.findFirstByUserAndTokenTypeAndUsedFalseOrderByIssuedAtDesc(
                testUser, VerificationToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(recentToken));

        assertThatThrownBy(() -> tokenService.resendToken(
                testUser, "http://localhost:8080", VerificationToken.TokenType.EMAIL_VERIFICATION))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Please wait");

        verify(verificationTokenRepository, never()).delete(any());
        verify(verificationTokenRepository, never()).save(any());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void resendToken_shouldSendToken_whenNoExistingToken() {
        when(verificationTokenRepository.findFirstByUserAndTokenTypeAndUsedFalseOrderByIssuedAtDesc(
                any(), any()))
                .thenReturn(Optional.empty());
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(emailTemplateService.buildVerificationEmailBody(anyString(), anyString()))
                .thenReturn("Email body");

        tokenService.resendToken(testUser, "http://localhost:8080",
                VerificationToken.TokenType.EMAIL_VERIFICATION);

        verify(verificationTokenRepository, never()).delete(any());
        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendEmail(eq("test@example.com"), anyString(), anyString());
    }

    @Test
    void resendToken_shouldHandlePasswordResetType() {
        when(verificationTokenRepository.findFirstByUserAndTokenTypeAndUsedFalseOrderByIssuedAtDesc(
                any(), any()))
                .thenReturn(Optional.empty());
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(emailTemplateService.buildPasswordResetEmailBody(anyString(), anyString()))
                .thenReturn("Reset email body");

        tokenService.resendToken(testUser, "http://localhost:8080",
                VerificationToken.TokenType.PASSWORD_RESET);

        verify(emailTemplateService).buildPasswordResetEmailBody(anyString(), anyString());
        verify(emailService).sendEmail(eq("test@example.com"), contains("Password Reset"),
                anyString());
    }

    @Test
    void cleanExpiredTokens_shouldDeleteExpiredTokens() {
        tokenService.cleanExpiredTokens();

        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(verificationTokenRepository).deleteExpiredTokens(instantCaptor.capture());

        Instant capturedInstant = instantCaptor.getValue();
        assertThat(capturedInstant).isNotNull();
        assertThat(capturedInstant).isBetween(
                Instant.now().minusSeconds(5),
                Instant.now().plusSeconds(5)
        );
    }

    @Test
    void cleanExpiredTokens_shouldBeScheduled() {
        tokenService.cleanExpiredTokens();
        tokenService.cleanExpiredTokens();
        tokenService.cleanExpiredTokens();

        verify(verificationTokenRepository, times(3)).deleteExpiredTokens(any(Instant.class));
    }

    @Test
    void getResendCooldownMinutes_shouldReturnConfiguredValue() {
        String result = tokenService.getResendCooldownMinutes();

        assertThat(result).isEqualTo("5");
    }

    @Test
    void getResendCooldownMinutes_shouldReturnCorrectValue_whenDifferentCooldown() {
        ReflectionTestUtils.setField(tokenService, "RESEND_COOLDOWN_MINUTES", 10L);

        String result = tokenService.getResendCooldownMinutes();

        assertThat(result).isEqualTo("10");
    }

    @Test
    void generateToken_shouldSetExpirationCorrectly() {
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(i -> i.getArgument(0));

        tokenService.generateToken(testUser, VerificationToken.TokenType.EMAIL_VERIFICATION);

        ArgumentCaptor<VerificationToken> captor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(verificationTokenRepository).save(captor.capture());

        VerificationToken saved = captor.getValue();
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(saved.getExpiresAt()).isBefore(LocalDateTime.now().plusSeconds(86400));
    }

    @Test
    void sendVerificationEmail_shouldHandleUserWithoutLastName() {
        testUser.setLastName(null);

        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(emailTemplateService.buildVerificationEmailBody(anyString(), anyString()))
                .thenReturn("Email body");

        tokenService.sendVerificationEmail(testUser, "http://localhost:8080");

        verify(emailTemplateService).buildVerificationEmailBody(
                eq("Ahmed null"),
                anyString()
        );
    }

    @Test
    void verifyToken_shouldHandleAlreadyActiveUser() {
        testUser.setActive(true);

        String rawToken = "valid-token";
        String hashedToken = "hashed-token";

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(hashedToken);
        verificationToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        verificationToken.setExpiresAt(LocalDateTime.now().plusSeconds(3600));
        verificationToken.setUser(testUser);

        when(verificationTokenRepository.findFirstByUserAndTokenTypeAndUsedFalseOrderByIssuedAtDesc(
                testUser, any()))
                .thenReturn(Optional.of(verificationToken));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result =
                tokenService.verifyToken(rawToken, VerificationToken.TokenType.EMAIL_VERIFICATION);

        assertThat(result.isActive()).isTrue();
        verify(verificationTokenRepository).delete(verificationToken);
    }
}