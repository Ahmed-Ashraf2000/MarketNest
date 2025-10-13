package com.marketnest.ecommerce.service.user;

import com.marketnest.ecommerce.dto.user.AccountActionDto;
import com.marketnest.ecommerce.dto.user.profile.ProfileRequestDto;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User updateProfile(String email, ProfileRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UserNotFoundException("User not found with email: " + email));

        if (requestDto.getFirstName() != null) {
            user.setFirstName(requestDto.getFirstName());
        }

        if (requestDto.getLastName() != null) {
            user.setLastName(requestDto.getLastName());
        }

        if (requestDto.getPhone() != null) {
            user.setPhone(requestDto.getPhone());
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updateProfilePhoto(String email, String photoUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UserNotFoundException("User not found with email: " + email));

        user.setPhotoUrl(photoUrl);
        return userRepository.save(user);
    }

    @Transactional
    public void processAccountAction(String email, AccountActionDto accountActionDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UserNotFoundException(
                                "User not found with this email: " + email));

        if (!passwordEncoder.matches(accountActionDto.getPasswordConfirmation(),
                user.getPassword())) {
            throw new BadCredentialsException("Invalid password confirmation");
        }

        switch (accountActionDto.getActionType()) {
            case DEACTIVATE:
                deactivateAccount(user);
                break;
            case DELETE:
                deleteAccount(user);
                break;
            default:
                throw new IllegalArgumentException("Invalid action type");
        }
    }

    private void deactivateAccount(User user) {
        user.setActive(false);
        userRepository.save(user);
    }

    private void deleteAccount(User user) {
        userRepository.delete(user);
    }
}