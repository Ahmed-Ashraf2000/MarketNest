package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.config.ApplicationContextProvider;
import com.marketnest.ecommerce.dto.auth.*;
import com.marketnest.ecommerce.dto.error.SimpleErrorResponse;
import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.mapper.auth.UserLoginMapper;
import com.marketnest.ecommerce.model.RefreshToken;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.model.User.Role;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.auth.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.marketnest.ecommerce.utils.AuthUtils.buildBaseUrl;
import static com.marketnest.ecommerce.utils.AuthUtils.extractRefreshTokenFromCookie;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final VerificationService verificationService;
    private final AuthenticationManager authenticationManager;
    private final UserLoginMapper userLoginMapper;
    private final RefreshTokenService refreshTokenService;
    private final LoginHistoryService loginHistoryService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDto loginDto) {
        try {

            Authentication authentication =
                    UsernamePasswordAuthenticationToken.unauthenticated(loginDto.getEmail(),
                            loginDto.getPassword());

            Authentication authenticationResponse =
                    authenticationManager.authenticate(authentication);

            SecurityContextHolder.getContext().setAuthentication(authenticationResponse);

            User user = userRepository.findByEmail(loginDto.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            String token = jwtService.generateToken(authenticationResponse);

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

            LoginResponseDto loginResponseDto = userLoginMapper.toLoginResponse(user);


            ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken.getToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .maxAge(refreshTokenService.getRefreshTokenDuration())
                    .path("/")
                    .build();

            ApplicationEventPublisher eventPublisher =
                    ApplicationContextProvider.getApplicationContext();
            eventPublisher.publishEvent(new AuthenticationSuccessEvent(authenticationResponse));

            return ResponseEntity.status(HttpStatus.OK)
                    .header("Authorization", "Bearer " + token)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(loginResponseDto);
        } catch (AuthenticationException e) {
            ApplicationEventPublisher eventPublisher =
                    ApplicationContextProvider.getApplicationContext();
            Authentication failedAuth = UsernamePasswordAuthenticationToken.unauthenticated(
                    loginDto.getEmail(), "[PROTECTED]");
            eventPublisher.publishEvent(new AuthenticationFailureBadCredentialsEvent(
                    failedAuth, e));

            return ResponseEntity.badRequest()
                    .body(new SimpleErrorResponse("Invalid username or password"));
        }
    }

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

        User savedUser = authService.registerUser(registrationDto, Role.CUSTOMER);

        String baseUrl = buildBaseUrl(request);

        verificationService.sendVerificationEmail(savedUser, baseUrl);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message",
                "Registration successful! Please check your email to verify your account.");
        response.put("email", savedUser.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        log.info(token);
        User user = verificationService.verifyToken(token);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
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

        verificationService.resendVerificationEmail(user, baseUrl);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Verification email has been resent");
        response.put("cooldownMinutes", verificationService.getResendCooldownMinutes());

        return ResponseEntity.ok(response);

    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String requestRefreshToken = extractRefreshTokenFromCookie(request);

        if (requestRefreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new SimpleErrorResponse("Refresh token is missing"));
        }

        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        User user = refreshToken.getUser();
        String email = user.getEmail();
        String authorities = "ROLE_" + user.getRole();
        String accessToken = jwtService.generateToken(email, authorities);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "New tokens issued successfully");

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + accessToken)
                .body(response);
    }

    @GetMapping("/login-history")
    public ResponseEntity<?> getLoginHistory(Authentication authentication) {
        String email = authentication.getName();
        List<LoginHistoryDto> loginHistory = loginHistoryService.getUserLoginHistory(email);
        return ResponseEntity.ok(loginHistory);
    }

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDto dto,
                                            Authentication authentication,
                                            BindingResult bindingResult,
                                            HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        String email = authentication.getName();

        authService.changePassword(
                email,
                dto.getCurrentPassword(),
                dto.getNewPassword()
        );

        SecurityContextHolder.clearContext();

        response.setHeader("Authorization", "");

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("status", "success");
        responseMap.put("message",
                "Password changed successfully. Please login again with your new password.");
        return ResponseEntity.ok(responseMap);
    }

}
