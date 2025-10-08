package com.marketnest.ecommerce.service.auth;

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

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final EmailTemplateService emailTemplateService;
    private final VerificationTokenRepository verificationTokenRepository;
    @Value("${verification.resend-cooldown-minutes}")
    private long RESEND_COOLDOWN_MINUTES;

    @Transactional
    public String generateEmailVerificationToken(User user) {
        String verificationToken = tokenService.generateRandomToken();
        String hashedToken = tokenService.hashToken(verificationToken);

        VerificationToken emailVerificationToken = new VerificationToken();
        emailVerificationToken.setToken(hashedToken);
        emailVerificationToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        emailVerificationToken.setExpiresAt(
                tokenService.calculateExpirationDate(TokenService.TOKEN_TYPE_EMAIL_VERIFICATION));
        emailVerificationToken.setUser(user);

        verificationTokenRepository.save(emailVerificationToken);

        return verificationToken;
    }

    @Transactional
    public void sendVerificationEmail(User user, String baseUrl) {
        String token = generateEmailVerificationToken(user);
        String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + token;

        String subject = "Verify Your Email";
        String body = emailTemplateService.buildVerificationEmailBody(
                user.getFirstName() + " " + user.getLastName(),
                verificationUrl
        );

        emailService.sendEmail(user.getEmail(), subject, body);
    }

    @Transactional
    public User verifyToken(String token) {
        String hashedToken = tokenService.hashToken(token);

        VerificationToken verificationToken = verificationTokenRepository.findByToken(hashedToken)
                .orElseThrow(() -> new RuntimeException("Invalid Email verification token"));

        if (!verificationToken.isValid()) {
            throw new RuntimeException("Email Verification token has expired or already used");
        }

        verificationToken.setUsed(true);
        verificationToken.getUser().setEmailVerified(true);
        verificationToken.getUser().setActive(true);

        verificationTokenRepository.save(verificationToken);
        return userRepository.save(verificationToken.getUser());
    }

    @Transactional
    public void resendVerificationEmail(User user, String baseUrl) {
        Optional<VerificationToken> existingToken = verificationTokenRepository
                .findFirstByUserAndTokenTypeAndUsedFalseOrderByIssuedAtDesc(
                        user,
                        VerificationToken.TokenType.EMAIL_VERIFICATION
                );

        VerificationToken token;
        String rawToken;

        if (existingToken.isPresent() && existingToken.get().isExpired()) {
            token = existingToken.get();

            if (!token.canResend(RESEND_COOLDOWN_MINUTES)) {
                long remainingSeconds = token.getRemainingCooldownSeconds(RESEND_COOLDOWN_MINUTES);
                throw new RuntimeException(
                        String.format(
                                "Please wait %d seconds before requesting another verification email",
                                remainingSeconds)
                );
            }

            verificationTokenRepository.save(token);

        }
        rawToken = generateEmailVerificationToken(user);

        String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + rawToken;

        String subject = "Verify Your Email";
        String body = emailTemplateService.buildVerificationEmailBody(
                user.getFirstName() + " " + user.getLastName(),
                verificationUrl
        );

        emailService.sendEmail(user.getEmail(), subject, body);
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