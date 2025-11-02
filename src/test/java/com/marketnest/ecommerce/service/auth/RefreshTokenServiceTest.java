package com.marketnest.ecommerce.service.auth;

import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.model.RefreshToken;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.RefreshTokenRepository;
import com.marketnest.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 604800000L);

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
    }

    @Test
    void createRefreshToken_shouldCreateToken_whenUserExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(
                i -> i.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken("test@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getExpiresAt()).isAfter(LocalDateTime.now());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.createRefreshToken("unknown@example.com"))
                .isInstanceOf(UserNotFoundException.class);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void findByToken_shouldReturnToken_whenExists() {
        RefreshToken token = new RefreshToken();
        token.setToken("test-token");
        when(refreshTokenRepository.findByToken("test-token")).thenReturn(Optional.of(token));

        Optional<RefreshToken> result = refreshTokenService.findByToken("test-token");

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("test-token");
    }

    @Test
    void verifyExpiration_shouldReturnToken_whenValid() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(LocalDateTime.now().plusDays(1));
        token.setRevoked(false);

        RefreshToken result = refreshTokenService.verifyExpiration(token);

        assertThat(result).isEqualTo(token);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void verifyExpiration_shouldThrowAndDelete_whenRevoked() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(LocalDateTime.now().plusDays(1));
        token.setRevoked(true);

        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(token))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("revoked");

        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void verifyExpiration_shouldThrowAndDelete_whenExpired() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(LocalDateTime.now().minusDays(1));
        token.setRevoked(false);

        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(token))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("expired");

        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void revokeAllUserTokens_shouldCallRepository() {
        refreshTokenService.revokeAllUserTokens(testUser);

        verify(refreshTokenRepository).revokeAllUserTokens(testUser);
    }

    @Test
    void getRefreshTokenDuration_shouldReturnDurationInSeconds() {
        long duration = refreshTokenService.getRefreshTokenDuration();

        assertThat(duration).isEqualTo(604800L); // 7 days in seconds
    }
}