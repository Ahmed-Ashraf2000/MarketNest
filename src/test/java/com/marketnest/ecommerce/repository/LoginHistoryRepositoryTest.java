package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.LoginHistory;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LoginHistoryRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

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
        loginHistoryRepository.deleteAll();
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
    void findByUserOrderByLoginTimestampDesc_shouldReturnHistoryInDescendingOrder() {
        LoginHistory login1 = loginHistoryRepository.save(
                createLoginHistory(testUser, Instant.now().minus(3, ChronoUnit.DAYS),
                        LoginHistory.LoginStatus.SUCCESS));
        LoginHistory login2 = loginHistoryRepository.save(
                createLoginHistory(testUser, Instant.now().minus(2, ChronoUnit.DAYS),
                        LoginHistory.LoginStatus.SUCCESS));
        LoginHistory login3 = loginHistoryRepository.save(
                createLoginHistory(testUser, Instant.now().minus(1, ChronoUnit.DAYS),
                        LoginHistory.LoginStatus.FAILED));

        List<LoginHistory> result =
                loginHistoryRepository.findByUserOrderByLoginTimestampDesc(testUser);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getLoginTimestamp()).isAfter(result.get(1).getLoginTimestamp());
        assertThat(result.get(1).getLoginTimestamp()).isAfter(result.get(2).getLoginTimestamp());
    }

    @Test
    void findByUserOrderByLoginTimestampDesc_shouldReturnEmptyList_whenNoHistory() {
        List<LoginHistory> result =
                loginHistoryRepository.findByUserOrderByLoginTimestampDesc(testUser);

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserOrderByLoginTimestampDesc_shouldReturnOnlyUserHistory() {
        loginHistoryRepository.save(
                createLoginHistory(testUser, Instant.now(), LoginHistory.LoginStatus.SUCCESS));
        loginHistoryRepository.save(
                createLoginHistory(otherUser, Instant.now(), LoginHistory.LoginStatus.SUCCESS));

        List<LoginHistory> result =
                loginHistoryRepository.findByUserOrderByLoginTimestampDesc(testUser);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUser().getUserId()).isEqualTo(testUser.getUserId());
    }

    @Test
    void findTopByUserAndStatusOrderByLoginTimestampDesc_shouldReturnMostRecentSuccessfulLogin() {
        loginHistoryRepository.save(
                createLoginHistory(testUser, Instant.now().minus(5, ChronoUnit.DAYS),
                        LoginHistory.LoginStatus.SUCCESS));
        LoginHistory recentSuccess = loginHistoryRepository.save(
                createLoginHistory(testUser, Instant.now().minus(1, ChronoUnit.DAYS),
                        LoginHistory.LoginStatus.SUCCESS));
        loginHistoryRepository.save(
                createLoginHistory(testUser, Instant.now(), LoginHistory.LoginStatus.FAILED));

        Optional<LoginHistory> result =
                loginHistoryRepository.findTopByUserAndStatusOrderByLoginTimestampDesc(
                        testUser, LoginHistory.LoginStatus.SUCCESS);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(recentSuccess.getId());
        assertThat(result.get().getStatus()).isEqualTo(LoginHistory.LoginStatus.SUCCESS);
    }

    @Test
    void findTopByUserAndStatusOrderByLoginTimestampDesc_shouldReturnEmpty_whenNoMatchingStatus() {
        loginHistoryRepository.save(
                createLoginHistory(testUser, Instant.now(), LoginHistory.LoginStatus.SUCCESS));

        Optional<LoginHistory> result =
                loginHistoryRepository.findTopByUserAndStatusOrderByLoginTimestampDesc(
                        testUser, LoginHistory.LoginStatus.FAILED);

        assertThat(result).isEmpty();
    }

    @Test
    void findTopByUserAndStatusOrderByLoginTimestampDesc_shouldReturnMostRecentFailedLogin() {
        loginHistoryRepository.save(
                createLoginHistory(testUser, Instant.now().minus(2, ChronoUnit.HOURS),
                        LoginHistory.LoginStatus.FAILED));
        LoginHistory recentFailed = loginHistoryRepository.save(
                createLoginHistory(testUser, Instant.now().minus(1, ChronoUnit.HOURS),
                        LoginHistory.LoginStatus.FAILED));
        loginHistoryRepository.save(
                createLoginHistory(testUser, Instant.now(), LoginHistory.LoginStatus.SUCCESS));

        Optional<LoginHistory> result =
                loginHistoryRepository.findTopByUserAndStatusOrderByLoginTimestampDesc(
                        testUser, LoginHistory.LoginStatus.FAILED);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(recentFailed.getId());
        assertThat(result.get().getStatus()).isEqualTo(LoginHistory.LoginStatus.FAILED);
    }

    @Test
    void findByUserAndStatusAndLoginTimestampAfter_shouldReturnMatchingLogins() {
        Instant cutoffTime = Instant.now().minus(1, ChronoUnit.HOURS);

        loginHistoryRepository.save(
                createLoginHistory(testUser, cutoffTime.minus(2, ChronoUnit.HOURS),
                        LoginHistory.LoginStatus.FAILED));
        loginHistoryRepository.save(
                createLoginHistory(testUser, cutoffTime.plus(10, ChronoUnit.MINUTES),
                        LoginHistory.LoginStatus.FAILED));
        loginHistoryRepository.save(
                createLoginHistory(testUser, cutoffTime.plus(20, ChronoUnit.MINUTES),
                        LoginHistory.LoginStatus.FAILED));
        loginHistoryRepository.save(
                createLoginHistory(testUser, cutoffTime.plus(15, ChronoUnit.MINUTES),
                        LoginHistory.LoginStatus.SUCCESS));

        List<LoginHistory> result =
                loginHistoryRepository.findByUserAndStatusAndLoginTimestampAfter(
                        testUser, LoginHistory.LoginStatus.FAILED, cutoffTime);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(login -> login.getStatus() == LoginHistory.LoginStatus.FAILED);
        assertThat(result).allMatch(login -> login.getLoginTimestamp().isAfter(cutoffTime));
    }

    @Test
    void findByUserAndStatusAndLoginTimestampAfter_shouldReturnEmpty_whenNoMatchingLogins() {
        Instant cutoffTime = Instant.now();

        loginHistoryRepository.save(
                createLoginHistory(testUser, cutoffTime.minus(1, ChronoUnit.DAYS),
                        LoginHistory.LoginStatus.FAILED));

        List<LoginHistory> result =
                loginHistoryRepository.findByUserAndStatusAndLoginTimestampAfter(
                        testUser, LoginHistory.LoginStatus.FAILED, cutoffTime);

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserAndStatusAndLoginTimestampAfter_shouldNotIncludeExactCutoffTime() {
        Instant cutoffTime = Instant.now();

        loginHistoryRepository.save(
                createLoginHistory(testUser, cutoffTime, LoginHistory.LoginStatus.FAILED));
        loginHistoryRepository.save(createLoginHistory(testUser, cutoffTime.plusSeconds(1),
                LoginHistory.LoginStatus.FAILED));

        List<LoginHistory> result =
                loginHistoryRepository.findByUserAndStatusAndLoginTimestampAfter(
                        testUser, LoginHistory.LoginStatus.FAILED, cutoffTime);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getLoginTimestamp()).isAfter(cutoffTime);
    }

    @Test
    void findByUserAndStatusAndLoginTimestampAfter_shouldHandleMultipleUsers() {
        Instant cutoffTime = Instant.now().minus(1, ChronoUnit.HOURS);

        loginHistoryRepository.save(
                createLoginHistory(testUser, Instant.now(), LoginHistory.LoginStatus.FAILED));
        loginHistoryRepository.save(
                createLoginHistory(otherUser, Instant.now(), LoginHistory.LoginStatus.FAILED));

        List<LoginHistory> result =
                loginHistoryRepository.findByUserAndStatusAndLoginTimestampAfter(
                        testUser, LoginHistory.LoginStatus.FAILED, cutoffTime);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUser().getUserId()).isEqualTo(testUser.getUserId());
    }

    @Test
    void save_shouldPersistLoginHistory() {
        LoginHistory loginHistory =
                createLoginHistory(testUser, Instant.now(), LoginHistory.LoginStatus.SUCCESS);

        LoginHistory saved = loginHistoryRepository.save(loginHistory);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getUserId()).isEqualTo(testUser.getUserId());
        assertThat(saved.getStatus()).isEqualTo(LoginHistory.LoginStatus.SUCCESS);
    }

    @Test
    void findByUserOrderByLoginTimestampDesc_shouldHandleSuspiciousLogins() {
        LoginHistory normalLogin =
                createLoginHistory(testUser, Instant.now().minus(2, ChronoUnit.DAYS),
                        LoginHistory.LoginStatus.SUCCESS);
        normalLogin.setSuspicious(false);
        normalLogin = loginHistoryRepository.save(normalLogin);

        LoginHistory suspiciousLogin =
                createLoginHistory(testUser, Instant.now().minus(1, ChronoUnit.DAYS),
                        LoginHistory.LoginStatus.SUCCESS);
        suspiciousLogin.setSuspicious(true);
        suspiciousLogin = loginHistoryRepository.save(suspiciousLogin);

        List<LoginHistory> result =
                loginHistoryRepository.findByUserOrderByLoginTimestampDesc(testUser);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).isSuspicious()).isTrue();
        assertThat(result.get(1).isSuspicious()).isFalse();
    }

    @Test
    void findByUserAndStatusAndLoginTimestampAfter_shouldReturnCorrectCount() {
        Instant cutoffTime = Instant.now().minus(10, ChronoUnit.MINUTES);

        for (int i = 0; i < 5; i++) {
            loginHistoryRepository.save(createLoginHistory(
                    testUser,
                    cutoffTime.plus(i + 1, ChronoUnit.MINUTES),
                    LoginHistory.LoginStatus.FAILED
            ));
        }

        List<LoginHistory> result =
                loginHistoryRepository.findByUserAndStatusAndLoginTimestampAfter(
                        testUser, LoginHistory.LoginStatus.FAILED, cutoffTime);

        assertThat(result).hasSize(5);
    }

    @Test
    void findTopByUserAndStatusOrderByLoginTimestampDesc_shouldHandleMultipleUsersWithSameStatus() {
        loginHistoryRepository.save(
                createLoginHistory(testUser, Instant.now().minus(1, ChronoUnit.HOURS),
                        LoginHistory.LoginStatus.SUCCESS));
        loginHistoryRepository.save(
                createLoginHistory(otherUser, Instant.now(), LoginHistory.LoginStatus.SUCCESS));

        Optional<LoginHistory> result =
                loginHistoryRepository.findTopByUserAndStatusOrderByLoginTimestampDesc(
                        testUser, LoginHistory.LoginStatus.SUCCESS);

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getUserId()).isEqualTo(testUser.getUserId());
    }

    @Test
    void findByUserAndStatusAndLoginTimestampAfter_shouldHandleMixedStatusesCorrectly() {
        Instant cutoffTime = Instant.now().minus(1, ChronoUnit.HOURS);

        loginHistoryRepository.save(
                createLoginHistory(testUser, cutoffTime.plus(10, ChronoUnit.MINUTES),
                        LoginHistory.LoginStatus.FAILED));
        loginHistoryRepository.save(
                createLoginHistory(testUser, cutoffTime.plus(20, ChronoUnit.MINUTES),
                        LoginHistory.LoginStatus.SUCCESS));
        loginHistoryRepository.save(
                createLoginHistory(testUser, cutoffTime.plus(30, ChronoUnit.MINUTES),
                        LoginHistory.LoginStatus.FAILED));

        List<LoginHistory> failedLogins =
                loginHistoryRepository.findByUserAndStatusAndLoginTimestampAfter(
                        testUser, LoginHistory.LoginStatus.FAILED, cutoffTime);
        List<LoginHistory> successLogins =
                loginHistoryRepository.findByUserAndStatusAndLoginTimestampAfter(
                        testUser, LoginHistory.LoginStatus.SUCCESS, cutoffTime);

        assertThat(failedLogins).hasSize(2);
        assertThat(successLogins).hasSize(1);
    }

    @Test
    void findAll_shouldReturnAllLoginHistory() {
        loginHistoryRepository.save(
                createLoginHistory(testUser, Instant.now(), LoginHistory.LoginStatus.SUCCESS));
        loginHistoryRepository.save(
                createLoginHistory(otherUser, Instant.now(), LoginHistory.LoginStatus.FAILED));

        List<LoginHistory> allHistory = loginHistoryRepository.findAll();

        assertThat(allHistory).hasSize(2);
    }

    private LoginHistory createLoginHistory(User user, Instant loginTimestamp,
                                            LoginHistory.LoginStatus status) {
        LoginHistory history = new LoginHistory();
        history.setUser(user);
        history.setIpAddress("192.168.1.1");
        history.setDeviceInfo("Chrome/Linux");
        history.setLocation("Cairo, EGY");
        history.setLoginTimestamp(loginTimestamp);
        history.setStatus(status);
        history.setSuspicious(false);
        return history;
    }
}