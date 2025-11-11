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
import com.marketnest.ecommerce.model.VerificationToken;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.auth.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import static com.marketnest.ecommerce.util.AuthUtils.buildBaseUrl;
import static com.marketnest.ecommerce.util.AuthUtils.extractRefreshTokenFromCookie;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "Endpoints for user authentication, registration, password management, and session handling"
)
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final UserLoginMapper userLoginMapper;
    private final RefreshTokenService refreshTokenService;
    private final LoginHistoryService loginHistoryService;


    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticate user with email and password. Returns JWT access token in Authorization header and refresh token in HTTP-only cookie"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDto.class)
                    ),
                    headers = {
                            @io.swagger.v3.oas.annotations.headers.Header(
                                    name = "Authorization",
                                    description = "JWT access token with Bearer prefix",
                                    schema = @Schema(type = "string",
                                            example = "Bearer eyJhbGciOiJIUzI1NiIs...")
                            ),
                            @io.swagger.v3.oas.annotations.headers.Header(
                                    name = "Set-Cookie",
                                    description = "HTTP-only secure refresh token cookie",
                                    schema = @Schema(type = "string")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid credentials",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SimpleErrorResponse.class)
                    )
            )
    })
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
                    .body(new SimpleErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Create a new customer account. Sends email verification link to the provided email address"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Registration successful. Verification email sent",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"status\":\"success\",\"message\":\"Registration successful! Please check your email to verify your account.\",\"email\":\"user@example.com\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation errors or email already registered",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(oneOf = {ValidationErrorResponse.class,
                                    SimpleErrorResponse.class})
                    )
            )
    })
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

        tokenService.sendVerificationEmail(savedUser, baseUrl);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message",
                "Registration successful! Please check your email to verify your account.");
        response.put("email", savedUser.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/verify-email")
    @Operation(
            summary = "Verify email address",
            description = "Verify user's email address using the token sent to their email"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email verified successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"status\":\"success\",\"message\":\"Email verified successfully! You can now log in to your account.\",\"Email\":\"user@example.com\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SimpleErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> verifyEmail(
            @Parameter(description = "Email verification token", required = true,
                    example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
            @RequestParam String token
    ) {
        User user = tokenService.verifyToken(token, VerificationToken.TokenType.EMAIL_VERIFICATION);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message",
                "Email verified successfully! You can now log in to your account.");
        response.put("Email", user.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-token")
    @Operation(
            summary = "Resend verification token",
            description = "Resend email verification or password reset token. Subject to cooldown period to prevent abuse"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token resent successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"status\":\"success\",\"message\":\"Verification email has been resent\",\"cooldownMinutes\":\"5\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request, email missing, already verified, or cooldown period active",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SimpleErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> resendToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Request body with email and optional token type",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"email\":\"user@example.com\",\"tokenType\":\"EMAIL_VERIFICATION\"}"
                            )
                    )
            )
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest
    ) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new SimpleErrorResponse("Email is required"));
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found with this email"));

        String tokenType = request.get("tokenType");
        VerificationToken.TokenType verificationTokenType =
                VerificationToken.TokenType.EMAIL_VERIFICATION;

        if (tokenType != null && tokenType.equalsIgnoreCase("PASSWORD_RESET")) {
            verificationTokenType = VerificationToken.TokenType.PASSWORD_RESET;
        } else {
            if (user.isEmailVerified()) {
                return ResponseEntity.badRequest()
                        .body(new SimpleErrorResponse("Email is already verified"));
            }
        }

        String baseUrl = buildBaseUrl(httpRequest);

        tokenService.resendToken(user, baseUrl, verificationTokenType);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message",
                verificationTokenType == VerificationToken.TokenType.EMAIL_VERIFICATION ?
                        "Verification email has been resent" :
                        "Password reset email has been resent");
        response.put("cooldownMinutes", tokenService.getResendCooldownMinutes());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    @Operation(
            summary = "Refresh access token",
            description = "Get a new JWT access token using the refresh token stored in HTTP-only cookie"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "New access token generated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"status\":\"success\",\"message\":\"New jwt issued successfully\"}"
                            )
                    ),
                    headers = @io.swagger.v3.oas.annotations.headers.Header(
                            name = "Authorization",
                            description = "New JWT access token with Bearer prefix",
                            schema = @Schema(type = "string",
                                    example = "Bearer eyJhbGciOiJIUzI1NiIs...")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token missing or invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SimpleErrorResponse.class)
                    )
            )
    })
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
        response.put("message", "New jwt issued successfully");

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + accessToken)
                .body(response);
    }

    @GetMapping("/login-history")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Get user login history",
            description = "Retrieve the authenticated user's login history including timestamps, IP addresses, and devices"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login history retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginHistoryDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - valid JWT required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SimpleErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> getLoginHistory(Authentication authentication) {
        String email = authentication.getName();
        List<LoginHistoryDto> loginHistory = loginHistoryService.getUserLoginHistory(email);
        return ResponseEntity.ok(loginHistory);
    }

    @PatchMapping("/change-password")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Change password",
            description = "Change the authenticated user's password. Requires current password verification. Invalidates all existing sessions"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password changed successfully. User needs to login again",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"status\":\"success\",\"message\":\"Password changed successfully. Please login again with your new password.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation errors or incorrect current password",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(oneOf = {ValidationErrorResponse.class,
                                    SimpleErrorResponse.class})
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - valid JWT required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SimpleErrorResponse.class)
                    )
            )
    })
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

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Request password reset",
            description = "Send password reset link to user's email address. Link expires after configured time"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset link sent successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\":\"Password reset link sent to email\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation errors",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordDto dto,
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

        String baseUrl = buildBaseUrl(request);

        authService.forgotPassword(dto.getEmail(), baseUrl);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset link sent to email");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password",
            description = "Reset user password using the token received via email. Token expires after configured time"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"status\":\"success\",\"message\":\"Password has been reset successfully. Please login with your new password.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation errors or invalid/expired token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(oneOf = {ValidationErrorResponse.class,
                                    SimpleErrorResponse.class})
                    )
            )
    })
    public ResponseEntity<?> resetPassword(
            @Parameter(description = "Password reset token from email", required = true,
                    example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
            @RequestParam String token,
            @Valid @RequestBody ResetPasswordDto dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        authService.resetPassword(token, dto.getPassword());

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message",
                "Password has been reset successfully. Please login with your new password.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "User logout",
            description = "Logout user by revoking all refresh tokens and clearing session. Clears refresh token cookie and Authorization header"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout successful",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"status\":\"success\",\"message\":\"Logged out successfully\"}"
                            )
                    ),
                    headers = {
                            @io.swagger.v3.oas.annotations.headers.Header(
                                    name = "Set-Cookie",
                                    description = "Clears the refresh_token cookie",
                                    schema = @Schema(type = "string")
                            ),
                            @io.swagger.v3.oas.annotations.headers.Header(
                                    name = "Authorization",
                                    description = "Clears the Authorization header",
                                    schema = @Schema(type = "string")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - valid JWT required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SimpleErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during logout",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SimpleErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> logout(Authentication authentication) {
        if (authentication != null) {
            String email = authentication.getName();
            refreshTokenService.revokeAllUserTokens(userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found")));

            SecurityContextHolder.clearContext();

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", "")
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .maxAge(0)
                    .path("/")
                    .build();

            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("status", "success");
            responseMap.put("message", "Logged out successfully");

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .header("Authorization", "")
                    .body(responseMap);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SimpleErrorResponse("Failed to process logout"));
    }
}