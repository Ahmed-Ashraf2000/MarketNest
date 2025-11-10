package com.marketnest.ecommerce.dto.auth;

import com.marketnest.ecommerce.annotation.FieldsValueMatch;
import com.marketnest.ecommerce.annotation.PasswordValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@FieldsValueMatch(
        field = "password",
        fieldToMatch = "confirmPassword"
)
@Schema(description = "Data Transfer Object for new user registration. Contains all necessary information for creating a new user account")
public class UserRegistrationDto {

    @NotBlank(message = "First name is required")
    @Size(min = 4, message = "First name must be at least 4 characters")
    @Schema(description = "User's first name. Must be at least 4 characters", example = "Ahmed",
            requiredMode = Schema.RequiredMode.REQUIRED, minLength = 4)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, message = "Last name must be at least 2 characters")
    @Schema(description = "User's last name. Must be at least 2 characters", example = "Ashraf",
            requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "User's email address. Must be unique and will be used as login identifier",
            example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED,
            format = "email")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9][0-9]{7,14}$", message = "Invalid phone number")
    @Schema(description = "User's phone number. Must be 8-15 digits",
            example = "+201234567890", requiredMode = Schema.RequiredMode.REQUIRED,
            pattern = "^\\+?[1-9][0-9]{7,14}$")
    private String phone;

    @NotBlank(message = "Password is required")
    @PasswordValidator
    @Schema(description = "User's password. Must meet security requirements",
            example = "SecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED,
            format = "password")
    private String password;

    @NotBlank(message = "Confirm password is required")
    @Schema(description = "Password confirmation. Must match password field",
            example = "SecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED,
            format = "password")
    private String confirmPassword;
}