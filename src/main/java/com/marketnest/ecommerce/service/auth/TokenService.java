package com.marketnest.ecommerce.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenService {

    public static final String TOKEN_TYPE_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String TOKEN_TYPE_EMAIL_VERIFICATION = "EMAIL_VERIFICATION";


    public String generateRandomToken() {
        byte[] randomBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(randomBytes);
        return bytesToHex(randomBytes);
    }


    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }


    public LocalDateTime calculateExpirationDate(String tokenType) {
        if (TOKEN_TYPE_PASSWORD_RESET.equals(tokenType)) {
            // 10 minutes for password reset
            return LocalDateTime.now().plusMinutes(10);
        } else if (TOKEN_TYPE_EMAIL_VERIFICATION.equals(tokenType)) {
            // 24 hours for email verification
            return LocalDateTime.now().plusHours(24);
        }
        throw new IllegalArgumentException("Invalid token type: " + tokenType);
    }


    public String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}