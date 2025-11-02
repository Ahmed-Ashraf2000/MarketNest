package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.RefreshToken;
import com.marketnest.ecommerce.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RefreshTokenRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User otherUser;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword123");
        testUser.setFirstName("Ahmed");
        testUser.setLastName("Ashraf");
        testUser.setActive(true);
        testUser = userRepository.save(testUser);

        otherUser = new User();
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("hashedPassword456");
        otherUser.setFirstName("Mohamed");
        otherUser.setLastName("Hany");
        otherUser.setActive(true);
        otherUser = userRepository.save(otherUser);
    }

    @Test
    void findByToken_shouldReturnToken_whenExists() {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken token =
                createRefreshToken(testUser, tokenValue, LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(token);

        Optional<RefreshToken> result = refreshTokenRepository.findByToken(tokenValue);

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(tokenValue);
        assertThat(result.get().getUser().getUserId()).isEqualTo(testUser.getUserId());
    }

    @Test
    void findByToken_shouldReturnEmpty_whenNotExists() {
        Optional<RefreshToken> result = refreshTokenRepository.findByToken("non-existent-token");

        assertThat(result).isEmpty();
    }

    @Test
    void findByToken_shouldReturnEmpty_whenTokenIsNull() {
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(null);

        assertThat(result).isEmpty();
    }

    @Test
    void findByToken_shouldReturnCorrectToken_whenMultipleTokensExist() {
        String token1 = UUID.randomUUID().toString();
        String token2 = UUID.randomUUID().toString();
        String token3 = UUID.randomUUID().toString();

        refreshTokenRepository.save(
                createRefreshToken(testUser, token1, LocalDateTime.now().plusDays(7)));
        refreshTokenRepository.save(
                createRefreshToken(testUser, token2, LocalDateTime.now().plusDays(7)));
        refreshTokenRepository.save(
                createRefreshToken(testUser, token3, LocalDateTime.now().plusDays(7)));

        Optional<RefreshToken> result = refreshTokenRepository.findByToken(token2);

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(token2);
    }

    @Test
    void revokeAllUserTokens_shouldRevokeAllTokens_forSpecificUser() {
        RefreshToken token1 = refreshTokenRepository.save(
                createRefreshToken(testUser, UUID.randomUUID().toString(),
                        LocalDateTime.now().plusDays(7)));
        RefreshToken token2 = refreshTokenRepository.save(
                createRefreshToken(testUser, UUID.randomUUID().toString(),
                        LocalDateTime.now().plusDays(7)));
        RefreshToken token3 = refreshTokenRepository.save(
                createRefreshToken(testUser, UUID.randomUUID().toString(),
                        LocalDateTime.now().plusDays(7)));

        refreshTokenRepository.revokeAllUserTokens(testUser);
        refreshTokenRepository.flush();

        RefreshToken updated1 = refreshTokenRepository.findById(token1.getId()).orElseThrow();
        RefreshToken updated2 = refreshTokenRepository.findById(token2.getId()).orElseThrow();
        RefreshToken updated3 = refreshTokenRepository.findById(token3.getId()).orElseThrow();

        assertThat(updated1.isRevoked()).isTrue();
        assertThat(updated2.isRevoked()).isTrue();
        assertThat(updated3.isRevoked()).isTrue();
    }

    @Test
    void revokeAllUserTokens_shouldNotAffectOtherUsers() {
        RefreshToken testUserToken = refreshTokenRepository.save(
                createRefreshToken(testUser, UUID.randomUUID().toString(),
                        LocalDateTime.now().plusDays(7)));
        RefreshToken otherUserToken = refreshTokenRepository.save(
                createRefreshToken(otherUser, UUID.randomUUID().toString(),
                        LocalDateTime.now().plusDays(7)));

        refreshTokenRepository.revokeAllUserTokens(testUser);
        refreshTokenRepository.flush();

        RefreshToken updatedTestToken =
                refreshTokenRepository.findById(testUserToken.getId()).orElseThrow();
        RefreshToken updatedOtherToken =
                refreshTokenRepository.findById(otherUserToken.getId()).orElseThrow();

        assertThat(updatedTestToken.isRevoked()).isTrue();
        assertThat(updatedOtherToken.isRevoked()).isFalse();
    }

    @Test
    void revokeAllUserTokens_shouldHandleAlreadyRevokedTokens() {
        RefreshToken token = createRefreshToken(testUser, UUID.randomUUID().toString(),
                LocalDateTime.now().plusDays(7));
        token.setRevoked(true);
        token = refreshTokenRepository.save(token);

        refreshTokenRepository.revokeAllUserTokens(testUser);
        refreshTokenRepository.flush();

        RefreshToken updated = refreshTokenRepository.findById(token.getId()).orElseThrow();

        assertThat(updated.isRevoked()).isTrue();
    }

    @Test
    void revokeAllUserTokens_shouldHandleUserWithNoTokens() {
        User userWithoutTokens = new User();
        userWithoutTokens.setEmail("notoken@example.com");
        userWithoutTokens.setPassword("password789");
        userWithoutTokens.setFirstName("Khaled");
        userWithoutTokens.setLastName("Omar");
        userWithoutTokens.setActive(true);
        userWithoutTokens = userRepository.save(userWithoutTokens);

        User finalUser = userWithoutTokens;
        assertThatCode(() -> {
            refreshTokenRepository.revokeAllUserTokens(finalUser);
            refreshTokenRepository.flush();
        }).doesNotThrowAnyException();
    }

    @Test
    void deleteExpiredTokens_shouldDeleteOnlyExpiredTokens() {
        RefreshToken expiredToken1 = refreshTokenRepository.save(
                createRefreshToken(testUser, UUID.randomUUID().toString(),
                        LocalDateTime.now().minusDays(1)));
        RefreshToken expiredToken2 = refreshTokenRepository.save(
                createRefreshToken(testUser, UUID.randomUUID().toString(),
                        LocalDateTime.now().minusHours(1)));
        RefreshToken validToken = refreshTokenRepository.save(
                createRefreshToken(testUser, UUID.randomUUID().toString(),
                        LocalDateTime.now().plusDays(7)));

        refreshTokenRepository.deleteExpiredTokens(Instant.now());
        refreshTokenRepository.flush();

        assertThat(refreshTokenRepository.findById(expiredToken1.getId())).isEmpty();
        assertThat(refreshTokenRepository.findById(expiredToken2.getId())).isEmpty();
        assertThat(refreshTokenRepository.findById(validToken.getId())).isPresent();
    }

    @Test
    void deleteExpiredTokens_shouldDeleteTokensAtExactExpirationTime() {
        Instant now = Instant.now();
        RefreshToken tokenExpiringNow = createRefreshToken(testUser, UUID.randomUUID().toString(),
                LocalDateTime.now().minusSeconds(1));
        tokenExpiringNow = refreshTokenRepository.save(tokenExpiringNow);

        refreshTokenRepository.deleteExpiredTokens(now);
        refreshTokenRepository.flush();

        assertThat(refreshTokenRepository.findById(tokenExpiringNow.getId())).isEmpty();
    }

    @Test
    void deleteExpiredTokens_shouldNotDeleteFutureTokens() {
        RefreshToken futureToken = refreshTokenRepository.save(
                createRefreshToken(testUser, UUID.randomUUID().toString(),
                        LocalDateTime.now().plusDays(30)));

        refreshTokenRepository.deleteExpiredTokens(Instant.now());
        refreshTokenRepository.flush();

        assertThat(refreshTokenRepository.findById(futureToken.getId())).isPresent();
    }

    @Test
    void deleteExpiredTokens_shouldHandleNoExpiredTokens() {
        refreshTokenRepository.save(createRefreshToken(testUser, UUID.randomUUID().toString(),
                LocalDateTime.now().plusDays(7)));
        refreshTokenRepository.save(createRefreshToken(testUser, UUID.randomUUID().toString(),
                LocalDateTime.now().plusDays(14)));

        long countBefore = refreshTokenRepository.count();

        refreshTokenRepository.deleteExpiredTokens(Instant.now());
        refreshTokenRepository.flush();

        long countAfter = refreshTokenRepository.count();

        assertThat(countAfter).isEqualTo(countBefore);
    }

    @Test
    void deleteExpiredTokens_shouldDeleteMultipleExpiredTokens() {
        for (int i = 0; i < 10; i++) {
            refreshTokenRepository.save(createRefreshToken(
                    testUser,
                    UUID.randomUUID().toString(),
                    LocalDateTime.now().minusDays(i + 1)
            ));
        }

        refreshTokenRepository.deleteExpiredTokens(Instant.now());
        refreshTokenRepository.flush();

        List<RefreshToken> remainingTokens = refreshTokenRepository.findAll();
        assertThat(remainingTokens).isEmpty();
    }

    @Test
    void save_shouldPersistRefreshToken() {
        RefreshToken token = createRefreshToken(testUser, UUID.randomUUID().toString(),
                LocalDateTime.now().plusDays(7));

        RefreshToken saved = refreshTokenRepository.save(token);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getToken()).isEqualTo(token.getToken());
        assertThat(saved.getUser().getUserId()).isEqualTo(testUser.getUserId());
    }

    @Test
    void findAll_shouldReturnAllTokens() {
        refreshTokenRepository.save(createRefreshToken(testUser, UUID.randomUUID().toString(),
                LocalDateTime.now().plusDays(7)));
        refreshTokenRepository.save(createRefreshToken(testUser, UUID.randomUUID().toString(),
                LocalDateTime.now().plusDays(7)));

        List<RefreshToken> tokens = refreshTokenRepository.findAll();

        assertThat(tokens).hasSize(2);
    }

    @Test
    void findByToken_shouldHandleSpecialCharactersInToken() {
        String specialToken = "token-with-special-chars-!@#$%^&*()";
        RefreshToken token =
                createRefreshToken(testUser, specialToken, LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(token);

        Optional<RefreshToken> result = refreshTokenRepository.findByToken(specialToken);

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(specialToken);
    }

    @Test
    void revokeAllUserTokens_shouldWorkWithTransactionalContext() {
        for (int i = 0; i < 5; i++) {
            refreshTokenRepository.save(createRefreshToken(testUser, UUID.randomUUID().toString(),
                    LocalDateTime.now().plusDays(7)));
        }

        refreshTokenRepository.revokeAllUserTokens(testUser);
        refreshTokenRepository.flush();

        List<RefreshToken> tokens = refreshTokenRepository.findAll();
        assertThat(tokens).allMatch(RefreshToken::isRevoked);
    }

    private RefreshToken createRefreshToken(User user, String tokenValue, LocalDateTime expiresAt) {
        RefreshToken token = new RefreshToken();
        token.setToken(tokenValue);
        token.setUser(user);
        token.setExpiresAt(expiresAt);
        token.setRevoked(false);
        return token;
    }
}