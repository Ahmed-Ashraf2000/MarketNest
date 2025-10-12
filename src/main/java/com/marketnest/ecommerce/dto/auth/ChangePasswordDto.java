package com.marketnest.ecommerce.dto.auth;

import com.marketnest.ecommerce.annotation.FieldsValueMatch;
import com.marketnest.ecommerce.annotation.PasswordValidator;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@FieldsValueMatch(
        field = "newPassword",
        fieldToMatch = "confirmPassword"
)
public class ChangePasswordDto {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @PasswordValidator
    private String newPassword;

    private String confirmPassword;
}
