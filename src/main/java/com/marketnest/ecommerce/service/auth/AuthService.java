package com.marketnest.ecommerce.service.auth;

import com.marketnest.ecommerce.dto.auth.UserRegistrationDto;
import com.marketnest.ecommerce.mapper.auth.UserRegisterMapper;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.email.EmailServiceImpl;
import com.marketnest.ecommerce.service.email.EmailTemplateService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailServiceImpl emailService;
    private final TokenService tokenService;
    private final EmailTemplateService emailTemplateService;
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
}
