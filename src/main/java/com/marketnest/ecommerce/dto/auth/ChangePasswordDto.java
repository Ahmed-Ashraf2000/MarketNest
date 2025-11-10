package com.marketnest.ecommerce.dto.auth;

import com.marketnest.ecommerce.annotation.FieldsValueMatch;
import com.marketnest.ecommerce.annotation.PasswordValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@FieldsValueMatch(
        field = "newPassword",
        fieldToMatch = "confirmPassword"
)
@Schema(description = "Data Transfer Object for changing user password")
public class ChangePasswordDto {

    @NotBlank(message = "Current password is required")
    @Schema(description = "User's current password for verification", example = "OldPass123!",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String currentPassword;

    @PasswordValidator
    @Schema(description = "New password that will replace the current password. Must meet password strength requirements",
            example = "NewPass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    @Schema(description = "Confirmation of the new password. Must match newPassword field",
            example = "NewPass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;
}