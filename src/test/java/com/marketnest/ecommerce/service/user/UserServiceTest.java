package com.marketnest.ecommerce.service.user;

import com.marketnest.ecommerce.dto.user.AccountActionDto;
import com.marketnest.ecommerce.dto.user.profile.ProfileRequestDto;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPassword("encodedPassword");
        testUser.setActive(true);
    }

    @Test
    void updateProfile_shouldUpdateUser_whenValidData() {
        ProfileRequestDto requestDto = new ProfileRequestDto();
        requestDto.setFirstName("Jane");
        requestDto.setLastName("Smith");
        requestDto.setPhone("+1234567890");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateProfile("test@example.com", requestDto);

        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getPhone()).isEqualTo("+1234567890");
        verify(userRepository).save(testUser);
    }

    @Test
    void updateProfile_shouldThrowException_whenUserNotFound() {
        ProfileRequestDto requestDto = new ProfileRequestDto();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile("unknown@example.com", requestDto))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfilePhoto_shouldUpdatePhoto_whenUserExists() {
        String photoUrl = "https://example.com/photo.jpg";
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateProfilePhoto("test@example.com", photoUrl);

        assertThat(result.getPhotoUrl()).isEqualTo(photoUrl);
        verify(userRepository).save(testUser);
    }

    @Test
    void processAccountAction_shouldDeactivateAccount_whenActionTypeIsDeactivate() {
        AccountActionDto dto = new AccountActionDto();
        dto.setActionType(AccountActionDto.ActionType.DEACTIVATE);
        dto.setPasswordConfirmation("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.processAccountAction("test@example.com", dto);

        assertThat(testUser.isActive()).isFalse();
        verify(userRepository).save(testUser);
    }

    @Test
    void processAccountAction_shouldDeleteAccount_whenActionTypeIsDelete() {
        AccountActionDto dto = new AccountActionDto();
        dto.setActionType(AccountActionDto.ActionType.DELETE);
        dto.setPasswordConfirmation("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

        userService.processAccountAction("test@example.com", dto);

        verify(userRepository).delete(testUser);
    }

    @Test
    void processAccountAction_shouldThrowException_whenPasswordIncorrect() {
        AccountActionDto dto = new AccountActionDto();
        dto.setActionType(AccountActionDto.ActionType.DEACTIVATE);
        dto.setPasswordConfirmation("wrongPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.processAccountAction("test@example.com", dto))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid password confirmation");

        verify(userRepository, never()).save(any());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void getAllUsers_shouldReturnPageOfUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser));
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<User> result = userService.getAllUsers(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testUser);
        verify(userRepository).findAll(pageable);
    }

    @Test
    void getUserById_shouldReturnUser_whenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.getUserById(1L);

        assertThat(result).isEqualTo(testUser);
    }

    @Test
    void getUserById_shouldThrowException_whenNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUserStatus_shouldUpdateStatus_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUserStatus(1L, false);

        assertThat(result.isActive()).isFalse();
        verify(userRepository).save(testUser);
    }

    @Test
    void deleteUserById_shouldDeleteUser_whenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.deleteUserById(1L);

        verify(userRepository).delete(testUser);
    }
}