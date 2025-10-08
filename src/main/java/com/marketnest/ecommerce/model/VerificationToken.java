package com.marketnest.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_tokens")
@Data
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    private TokenType tokenType;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "last_sent_at")
    private LocalDateTime lastSentAt;

    @Column(nullable = false)
    private boolean used = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }

    public boolean canResend(long cooldownMinutes) {
        if (lastSentAt == null) {
            return true;
        }
        long minutesSinceLastSent = Duration.between(lastSentAt, LocalDateTime.now()).toMinutes();
        return minutesSinceLastSent >= cooldownMinutes;
    }

    public long getRemainingCooldownSeconds(long cooldownMinutes) {
        if (lastSentAt == null) {
            return 0;
        }
        long secondsSinceLastSent = Duration.between(lastSentAt, LocalDateTime.now()).getSeconds();
        long cooldownSeconds = cooldownMinutes * 60;
        return Math.max(0, cooldownSeconds - secondsSinceLastSent);
    }

    public void markAsUsed() {
        this.used = true;
    }

    @PrePersist
    protected void onCreate() {
        if (issuedAt == null) {
            issuedAt = LocalDateTime.now();
        }
        if (lastSentAt == null) {
            lastSentAt = issuedAt;
        }
    }

    public enum TokenType {
        EMAIL_VERIFICATION, PASSWORD_RESET
    }
}
