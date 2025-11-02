package com.marketnest.ecommerce.service.auth;

import com.marketnest.ecommerce.dto.auth.UserRegistrationDto;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.mapper.auth.UserRegisterMapper;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRegisterMapper userRegisterMapper;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UserRegistrationDto registrationDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("Password_123");
        testUser.setFirstName("Ahmed");
        testUser.setLastName("Ashraf");
        testUser.setRole(User.Role.CUSTOMER);

        registrationDto = new UserRegistrationDto();
        registrationDto.setEmail("test@example.com");
        registrationDto.setPassword("Password_123");
        registrationDto.setFirstName("Ahmed");
        registrationDto.setLastName("Ashraf");
    }

    @Test
    void registerUser_shouldCreateNewUser_whenValidData() {
        when(userRegisterMapper.toEntity(registrationDto)).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.registerUser(registrationDto, User.Role.CUSTOMER);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(User.Role.CUSTOMER);
        assertThat(result.isActive()).isFalse();
        assertThat(result.isEmailVerified()).isFalse();
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(anyString());
    }

    @Test
    void changePassword_shouldUpdatePassword_whenValidCurrentPassword() {
        String currentPassword = "oldPassword";
        String newPassword = "newPassword123";

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        authService.changePassword(testUser.getEmail(), currentPassword, newPassword);

        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("encodedNewPassword") &&
                user.getPasswordChangedAt() != null
        ));
    }

    @Test
    void changePassword_shouldThrowException_whenInvalidCurrentPassword() {
        String currentPassword = "wrongPassword";
        String newPassword = "newPassword123";

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(false);

        assertThatThrownBy(
                () -> authService.changePassword(testUser.getEmail(), currentPassword, newPassword))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid current password");

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.changePassword("unknown@example.com", "old", "new"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void forgotPassword_shouldSendResetEmail_whenUserExists() {
        String baseUrl = "http://localhost:8080";
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        authService.forgotPassword(testUser.getEmail(), baseUrl);

        verify(tokenService).sendPasswordResetEmail(testUser, baseUrl);
    }

    @Test
    void forgotPassword_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> authService.forgotPassword("unknown@example.com", "http://localhost"))
                .isInstanceOf(UserNotFoundException.class);

        verify(tokenService, never()).sendPasswordResetEmail(any(), anyString());
    }

    @Test
    void resetPassword_shouldUpdatePassword_whenValidToken() {
        String token = "valid-token";
        String newPassword = "newPassword123";

        testUser.setPassword("oldEncodedPassword");
        when(tokenService.verifyToken(anyString(), any())).thenReturn(testUser);
        when(passwordEncoder.matches(newPassword, testUser.getPassword())).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        authService.resetPassword(token, newPassword);

        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("encodedNewPassword") &&
                user.getPasswordChangedAt() != null
        ));
    }

    @Test
    void resetPassword_shouldThrowException_whenNewPasswordSameAsOld() {
        String token = "valid-token";
        String newPassword = "samePassword";

        when(tokenService.verifyToken(anyString(), any())).thenReturn(testUser);
        when(passwordEncoder.matches(newPassword, testUser.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> authService.resetPassword(token, newPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same as the old password");

        verify(userRepository, never()).save(any());
    }
}