package com.marketnest.ecommerce.service.auth;

import com.marketnest.ecommerce.exception.InvalidVerificationToken;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.model.VerificationToken;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.repository.VerificationTokenRepository;
import com.marketnest.ecommerce.service.email.EmailService;
import com.marketnest.ecommerce.service.email.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static com.marketnest.ecommerce.utils.AuthUtils.*;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final VerificationTokenRepository verificationTokenRepository;

    @Value("${verification.resend-cooldown-minutes}")
    private long RESEND_COOLDOWN_MINUTES;

    @Transactional
    public String generateToken(User user, VerificationToken.TokenType tokenType) {
        String rawToken = generateRandomToken();
        String hashedToken = hashToken(rawToken);

        String tokenTypeStr = tokenType == VerificationToken.TokenType.EMAIL_VERIFICATION
                ? TOKEN_TYPE_EMAIL_VERIFICATION : TOKEN_TYPE_PASSWORD_RESET;

        VerificationToken token = new VerificationToken();
        token.setToken(hashedToken);
        token.setTokenType(tokenType);
        token.setExpiresAt(calculateExpirationDate(tokenTypeStr));
        token.setUser(user);

        verificationTokenRepository.save(token);

        return rawToken;
    }

    @Transactional
    public void sendVerificationEmail(User user, String baseUrl) {
        String token = generateToken(user, VerificationToken.TokenType.EMAIL_VERIFICATION);
        String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + token;

        String subject = "Verify Your Email";
        String body = emailTemplateService.buildVerificationEmailBody(
                user.getFirstName() + " " + user.getLastName(),
                verificationUrl
        );

        emailService.sendEmail(user.getEmail(), subject, body);
    }

    @Transactional
    public void sendPasswordResetEmail(User user, String baseUrl) {
        String token = generateToken(user, VerificationToken.TokenType.PASSWORD_RESET);
        String resetUrl = baseUrl + "/api/auth/reset-password?token=" + token;

        String subject = "Password Reset Request";
        String body = emailTemplateService.buildPasswordResetEmailBody(
                user.getFirstName() + " " + user.getLastName(),
                resetUrl
        );

        emailService.sendEmail(user.getEmail(), subject, body);

    }

    @Transactional
    public User verifyToken(String token, VerificationToken.TokenType tokenType) {
        String hashedToken = hashToken(token);

        VerificationToken verificationToken = verificationTokenRepository.findByToken(hashedToken)
                .orElseThrow(() -> new InvalidVerificationToken("Invalid token"));

        if (!verificationToken.isValid() || verificationToken.getTokenType() != tokenType) {
            throw new InvalidVerificationToken(
                    "Token has expired, already used, or is of wrong type");
        }

        verificationToken.setUsed(true);

        if (tokenType == VerificationToken.TokenType.EMAIL_VERIFICATION) {
            verificationToken.getUser().setEmailVerified(true);
            verificationToken.getUser().setActive(true);
        }

        verificationTokenRepository.save(verificationToken);
        return userRepository.save(verificationToken.getUser());
    }

    @Transactional
    public void resendToken(User user, String baseUrl, VerificationToken.TokenType tokenType) {
        Optional<VerificationToken> existingToken = verificationTokenRepository
                .findFirstByUserAndTokenTypeAndUsedFalseOrderByIssuedAtDesc(
                        user, tokenType
                );

        if (existingToken.isPresent() && !existingToken.get().canResend(RESEND_COOLDOWN_MINUTES)) {
            long remainingSeconds =
                    existingToken.get().getRemainingCooldownSeconds(RESEND_COOLDOWN_MINUTES);
            throw new RuntimeException(
                    String.format("Please wait %d seconds before requesting another token",
                            remainingSeconds)
            );
        }

        if (tokenType == VerificationToken.TokenType.EMAIL_VERIFICATION) {
            sendVerificationEmail(user, baseUrl);
        } else if (tokenType == VerificationToken.TokenType.PASSWORD_RESET) {
            sendPasswordResetEmail(user, baseUrl);
        }
    }

    @Scheduled(fixedRate = 86400000)
    @Transactional
    public void cleanExpiredTokens() {
        verificationTokenRepository.deleteExpiredTokens(Instant.now());
    }

    public String getResendCooldownMinutes() {
        return String.valueOf(RESEND_COOLDOWN_MINUTES);
    }
}