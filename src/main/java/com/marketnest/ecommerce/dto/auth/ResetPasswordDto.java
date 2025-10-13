package com.marketnest.ecommerce.dto.auth;

import com.marketnest.ecommerce.annotation.FieldsValueMatch;
import com.marketnest.ecommerce.annotation.PasswordValidator;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@FieldsValueMatch(
        field = "password",
        fieldToMatch = "confirmPassword"
)
public class ResetPasswordDto {
    @NotBlank(message = "Password is required")
    @PasswordValidator
    private String password;

    private String confirmPassword;
}