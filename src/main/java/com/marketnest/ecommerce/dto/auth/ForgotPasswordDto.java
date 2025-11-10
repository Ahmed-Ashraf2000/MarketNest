package com.marketnest.ecommerce.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for initiating password recovery process")
public class ForgotPasswordDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    @Schema(description = "Email address associated with the user's account. A password reset link will be sent to this email",
            example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}