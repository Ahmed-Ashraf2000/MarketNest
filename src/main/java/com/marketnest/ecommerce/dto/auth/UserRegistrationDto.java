package com.marketnest.ecommerce.dto.auth;

import com.marketnest.ecommerce.annotation.FieldsValueMatch;
import com.marketnest.ecommerce.annotation.PasswordValidator;
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
public class UserRegistrationDto {

    @NotBlank(message = "First name is required")
    @Size(min = 4, message = "First name must be at least 4 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, message = "Last name must be at least 2 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9][0-9]{7,14}$", message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "Password is required")
    @PasswordValidator
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
