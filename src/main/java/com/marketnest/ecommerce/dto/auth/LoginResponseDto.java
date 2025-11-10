package com.marketnest.ecommerce.dto.auth;

import com.marketnest.ecommerce.model.User.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for successful login response. Contains user information returned after authentication")
public class LoginResponseDto {
    @Schema(description = "Unique identifier of the authenticated user", example = "1")
    private Long userId;

    @Schema(description = "User's first name", example = "Ahmed")
    private String firstName;

    @Schema(description = "User's last name", example = "Ashraf")
    private String lastName;

    @Schema(description = "User's phone number", example = "+201234567890")
    private String phone;

    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @Schema(description = "User's role in the system. Used for authorization and access control",
            example = "USER")
    private Role role;

    @Schema(description = "URL to the user's profile photo",
            example = "https://example.com/photos/user.jpg", nullable = true)
    private String photoUrl;

    @Schema(description = "Timestamp of the user's last successful login",
            example = "2025-01-15T10:30:00")
    private LocalDateTime lastLoginAt;
}