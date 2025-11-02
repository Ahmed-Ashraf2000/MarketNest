package com.marketnest.ecommerce.service.auth;

import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtService jwtService;

    private User testUser;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "jwtSecret",
                "testSecretKeyThatIsLongEnoughForHS256Algorithm");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 900000L);

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setRole(User.Role.CUSTOMER);
        testUser.setActive(true);

        authentication = new UsernamePasswordAuthenticationToken(
                "test@example.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }

    @Test
    void generateToken_shouldReturnValidToken_whenAuthenticationProvided() {
        String token = jwtService.generateToken(authentication);

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void generateToken_shouldIncludeTokenId_whenProvided() {
        String tokenId = "test-token-id";
        String token = jwtService.generateToken(authentication, tokenId);

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void generateToken_withEmailAndAuthorities_shouldReturnValidToken() {
        String token = jwtService.generateToken("test@example.com", "ROLE_CUSTOMER");

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void validateToken_shouldReturnAuthentication_whenTokenIsValid() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        String token = jwtService.generateToken(authentication);
        Authentication result = jwtService.validateToken(token);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("test@example.com");
        assertThat(result.getAuthorities()).hasSize(1);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void validateToken_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        String token = jwtService.generateToken(authentication);

        assertThatThrownBy(() -> jwtService.validateToken(token))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("disabled or not found");
    }

    @Test
    void validateToken_shouldThrowException_whenUserIsInactive() {
        testUser.setActive(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        String token = jwtService.generateToken(authentication);

        assertThatThrownBy(() -> jwtService.validateToken(token))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("disabled or not found");
    }

    @Test
    void validateToken_shouldThrowException_whenPasswordChangedAfterTokenIssued() throws Exception {
        testUser.setPasswordChangedAt(Instant.now().plusSeconds(10));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        String token = jwtService.generateToken(authentication);
        Thread.sleep(100);

        assertThatThrownBy(() -> jwtService.validateToken(token))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Password has been changed");
    }
}