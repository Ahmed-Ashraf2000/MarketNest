package com.marketnest.ecommerce.service.auth;

import com.marketnest.ecommerce.mapper.auth.UserRegisterMapper;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.email.EmailServiceImpl;
import com.marketnest.ecommerce.service.email.EmailTemplateService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailServiceImpl emailService;
    private final EmailTemplateService emailTemplateService;
    private final UserRegisterMapper userRegisterMapper;

//    @Transactional
//    public void changePassword(String email, String currentPassword, String newPassword) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException(
//                        "User belongs to this email " + email + " couldn't be found : "));
//
//        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
//            throw new BadCredentialsException("Invalid current password");
//        }
//
//        String encodedPassword = passwordEncoder.encode(newPassword);
//
//        user.setPassword(encodedPassword);
//        user.setPasswordChangedAt(Instant.now());
//
//        userRepository.save(user);
//    }

//    @Transactional
//    public String createResetPasswordToken(User user) {
//        String resetToken = tokenService.generateRandomToken();
//        String hashedToken = tokenService.hashToken(resetToken);
//
//        user.setPasswordResetToken(hashedToken);
//        user.setPasswordResetTokenExpireAT(
//                tokenService.calculateExpirationDate(TokenService.TOKEN_TYPE_PASSWORD_RESET));
//
//        userRepository.save(user);
//
//        return resetToken;
//    }

//    @Transactional
//    public void forgotPassword(String email, String baseUrl) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("No user found with email: " + email));
//
//        String resetToken = createResetPasswordToken(user);
//        String resetUrl = baseUrl + "/api/users/resetPassword/" + resetToken;
//
//        String subject = "Password Reset Request";
//        String body = emailTemplateService.buildPasswordResetEmailBody(
//                user.getFirstName() + " " + user.getLastName(),
//                resetUrl
//        );
//
//        emailService.sendEmail(user.getEmail(), subject, body);
//    }


//    @Transactional
//    public User resetPassword(String token, String newPassword) {
//        String hashedToken = tokenService.hashToken(token);
//
//        User user = userRepository.findByPasswordResetToken(hashedToken)
//                .orElseThrow(() -> new RuntimeException("Invalid or expired password reset token"));
//
//        if (user.getPasswordResetTokenExpireAT().isBefore(LocalDateTime.now())) {
//            throw new RuntimeException("Password reset token has expired");
//        }
//
//        String encodedPassword = passwordEncoder.encode(newPassword);
//        user.setPassword(encodedPassword);
//        user.setPasswordChangedAt(Instant.now().minusMillis(1000));
//
//        user.setPasswordResetToken(null);
//        user.setPasswordResetTokenExpireAT(null);
//
//        return userRepository.save(user);
//    }

//    @Transactional
//    public void processAccountAction(String username, AccountActionDto accountActionDto) {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("User not found: " + username));
//
//        if (!passwordEncoder.matches(accountActionDto.getPasswordConfirmation(),
//                user.getPassword())) {
//            throw new BadCredentialsException("Invalid password confirmation");
//        }
//
//        switch (accountActionDto.getActionType()) {
//            case DEACTIVATE:
//                deactivateAccount(user);
//                break;
//            case DELETE:
//                deleteAccount(user);
//                break;
//            default:
//                throw new IllegalArgumentException("Invalid action type");
//        }
//    }
//
//    private void deactivateAccount(User user) {
//        user.setActive(false);
//        userRepository.save(user);
//    }
//
//    private void deleteAccount(User user) {
//        userRepository.delete(user);
//    }
//
//    @Transactional
//    public User updateProfile(String username, ProfileUpdateDto profileDto) {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("User not found: " + username));
//
//        boolean changed = false;
//
//        if (profileDto.getEmail() != null && !profileDto.getEmail().equals(user.getEmail())) {
//            if (userRepository.existsByEmail(profileDto.getEmail())) {
//                throw new RuntimeException("Email already in use");
//            }
//            user.setEmail(profileDto.getEmail());
//            changed = true;
//        }
//
//        if (profileDto.getUsername() != null && !profileDto.getUsername()
//                .equals(user.getUsername())) {
//            if (userRepository.existsByUsername(profileDto.getUsername())) {
//                throw new RuntimeException("Username already in use");
//            }
//            user.setUsername(profileDto.getUsername());
//            changed = true;
//        }
//
//        if (changed) {
//            userRepository.updateUserNameAndEmail(user.getUserId(), user.getUsername(),
//                    user.getEmail());
//            return userRepository.findById(user.getUserId()).orElse(user);
//        }
//
//        return user;
//    }
//
//
//    @Transactional
//    public User updateProfilePhoto(String username, MultipartFile photo) throws IOException {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("User not found: " + username));
//
//        if (photo.isEmpty()) {
//            throw new IllegalArgumentException("Photo cannot be empty");
//        }
//
//        String contentType = photo.getContentType();
//        if (contentType == null || !contentType.startsWith("image/")) {
//            throw new IllegalArgumentException("File must be an image");
//        }
//
//        if (photo.getSize() > 2 * 1024 * 1024) {
//            throw new IllegalArgumentException("File size must be less than 2MB");
//        }
//
//        byte[] photoBytes = photo.getBytes();
//        user.setProfilePhoto(photoBytes);
//        user.setProfilePhotoFilename(photo.getOriginalFilename());
//        user.setProfilePhotoContentType(contentType);
//
//        return userRepository.save(user);
//    }
//
//    @Transactional
//    public void deleteProfilePhoto(String username) {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("User not found: " + username));
//
//        user.setProfilePhoto(null);
//        user.setProfilePhotoFilename(null);
//        user.setProfilePhotoContentType(null);
//
//        userRepository.save(user);
//    }
}