package com.marketnest.ecommerce.service.auth;

import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.model.VerificationToken;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.repository.VerificationTokenRepository;
import com.marketnest.ecommerce.service.email.EmailService;
import com.marketnest.ecommerce.service.email.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final EmailTemplateService emailTemplateService;
    private final VerificationTokenRepository verificationTokenRepository;

    @Transactional
    public String generateEmailVerificationToken() {
        String verificationToken = tokenService.generateRandomToken();
        String hashedToken = tokenService.hashToken(verificationToken);

        VerificationToken emailVerificationToken = new VerificationToken();
        emailVerificationToken.setToken(hashedToken);
        emailVerificationToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        emailVerificationToken.setExpiresAt(
                tokenService.calculateExpirationDate(TokenService.TOKEN_TYPE_EMAIL_VERIFICATION));

        return verificationToken;
    }

    @Transactional
    public void sendVerificationEmail(User user, String baseUrl) {
        String token = generateEmailVerificationToken();
        String verificationUrl = baseUrl + "/api/auth/verifyEmail/" + token;

        String subject = "Verify Your Email";
        String body = emailTemplateService.buildVerificationEmailBody(
                user.getFirstName() + " " + user.getLastName(),
                verificationUrl
        );

        emailService.sendEmail(user.getEmail(), subject, body);
    }

    @Transactional
    public User verifyEmail(String token) {
        String hashedToken = tokenService.hashToken(token);

        VerificationToken verificationToken = verificationTokenRepository.findByToken(hashedToken)
                .orElseThrow(() -> new RuntimeException("Invalid Email verification token"));

        if (!verificationToken.isValid()) {
            throw new RuntimeException("Email Verification token has expired or already used");
        }

        verificationToken.getUser().setEmailVerified(true);
        verificationToken.getUser().setActive(true);

        return userRepository.save(verificationToken.getUser());
    }
}