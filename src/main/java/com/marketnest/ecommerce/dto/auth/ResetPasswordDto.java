package com.marketnest.ecommerce.dto.auth;

import com.marketnest.ecommerce.annotation.FieldsValueMatch;
import com.marketnest.ecommerce.annotation.PasswordValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@FieldsValueMatch(
        field = "password",
        fieldToMatch = "confirmPassword"
)
@Schema(description = "Data Transfer Object for resetting a forgotten password using a reset token")
public class ResetPasswordDto {
    @NotBlank(message = "Password is required")
    @PasswordValidator
    @Schema(description = "New password to set for the user account. Must meet password strength requirements",
            example = "NewSecure123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "Confirmation of the new password. Must match password field",
            example = "NewSecure123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;
}