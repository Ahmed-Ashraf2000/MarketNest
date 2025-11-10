package com.marketnest.ecommerce.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for user login credentials")
public class UserLoginDto {
    @NotBlank(message = "Email is required")
    @Schema(description = "User's email address used as login identifier",
            example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "User's password for authentication", example = "SecurePass123!",
            requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
    private String password;
}