package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.auth.UserRegistrationDto;
import com.marketnest.ecommerce.dto.error.SimpleErrorResponse;
import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.model.User.Role;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.auth.JwtService;
import com.marketnest.ecommerce.service.auth.UserService;
import com.marketnest.ecommerce.service.auth.VerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.marketnest.ecommerce.utils.AuthUtils.buildBaseUrl;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final VerificationService verificationService;


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto,
                                          BindingResult bindingResult,
                                          HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new SimpleErrorResponse("Email already registered"));
        }

        User savedUser = userService.registerUser(registrationDto, Role.CUSTOMER);

        String baseUrl = buildBaseUrl(request);

        verificationService.sendVerificationEmail(savedUser, baseUrl);

        Map<String, String> response = new HashMap<>();
        response.put("message",
                "Registration successful! Please check your email to verify your account.");
        response.put("email", savedUser.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/verifyEmail/{token}")
    public ResponseEntity<?> verifyEmail(@PathVariable String token) {
        User user = verificationService.verifyToken(token);

        Map<String, String> response = new HashMap<>();
        response.put("message",
                "Email verified successfully! You can now log in to your account.");
        response.put("Email", user.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> request,
                                                HttpServletRequest httpRequest) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new SimpleErrorResponse("Email is required"));
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found with this email"));

        if (user.isEmailVerified()) {
            return ResponseEntity.badRequest()
                    .body(new SimpleErrorResponse("Email is already verified"));
        }

        String baseUrl = buildBaseUrl(httpRequest);

        verificationService.sendVerificationEmail(user, baseUrl);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Verification email has been resent");

        return ResponseEntity.ok(response);

    }

}
