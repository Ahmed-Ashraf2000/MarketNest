package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findFirstByUserAndTokenTypeAndUsedFalseOrderByIssuedAtDesc(
            User user,
            VerificationToken.TokenType tokenType
    );

    @Modifying
    @Query("DELETE FROM VerificationToken r WHERE r.expiresAt < :now")
    void deleteExpiredTokens(Instant now);
}
