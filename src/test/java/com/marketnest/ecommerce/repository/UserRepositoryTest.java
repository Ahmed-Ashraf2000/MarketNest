package com.marketnest.ecommerce.repository;

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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    @Autowired
    private UserRepository userRepository;
    private User testUser;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(User.Role.CUSTOMER);
        testUser.setActive(true);
        testUser.setEmailVerified(true);
    }

    @Test
    void findByEmail_shouldReturnUser_whenExists() {
        userRepository.save(testUser);

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenNotExists() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void countByRole_shouldReturnCorrectCount() {
        userRepository.save(testUser);

        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(User.Role.ADMIN);
        adminUser.setActive(true);
        userRepository.save(adminUser);

        Long customerCount = userRepository.countByRole(User.Role.CUSTOMER);
        Long adminCount = userRepository.countByRole(User.Role.ADMIN);

        assertThat(customerCount).isEqualTo(1L);
        assertThat(adminCount).isEqualTo(1L);
    }

    @Test
    void countNewCustomersSince_shouldReturnCorrectCount() {
        testUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(testUser);

        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        Long count = userRepository.countNewCustomersSince(oneDayAgo);

        assertThat(count).isEqualTo(1L);
    }

    @Test
    void countActiveCustomers_shouldReturnCorrectCount() {
        testUser.setActive(true);
        userRepository.save(testUser);

        User inactiveUser = new User();
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setPassword("password");
        inactiveUser.setFirstName("Inactive");
        inactiveUser.setLastName("User");
        inactiveUser.setRole(User.Role.CUSTOMER);
        inactiveUser.setActive(false);
        userRepository.save(inactiveUser);

        Long count = userRepository.countActiveCustomers();

        assertThat(count).isEqualTo(1L);
    }
}