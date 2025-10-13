package com.marketnest.ecommerce.service.auth;

import com.marketnest.ecommerce.dto.auth.UserRegistrationDto;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.mapper.auth.UserRegisterMapper;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.model.VerificationToken;
import com.marketnest.ecommerce.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@AllArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserRegisterMapper userRegisterMapper;

    @Transactional
    public User registerUser(UserRegistrationDto registrationDto, User.Role roleName) {
        User user = userRegisterMapper.toEntity(registrationDto);

        String encodedPassword = passwordEncoder.encode(user.getPassword());

        user.setPassword(encodedPassword);
        user.setActive(false);
        user.setEmailVerified(false);
        user.setRole(roleName);

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User belongs to this email " + email + " couldn't be found : "));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid current password");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);

        user.setPassword(encodedPassword);
        user.setPasswordChangedAt(Instant.now());

        userRepository.save(user);
    }

    @Transactional
    public void forgotPassword(String email, String baseUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("No user found with email: " + email));

        tokenService.sendPasswordResetEmail(user, baseUrl);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = tokenService.verifyToken(token, VerificationToken.TokenType.PASSWORD_RESET);

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException(
                    "New password cannot be the same as the old password");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setPasswordChangedAt(Instant.now());

        userRepository.save(user);
    }

}
