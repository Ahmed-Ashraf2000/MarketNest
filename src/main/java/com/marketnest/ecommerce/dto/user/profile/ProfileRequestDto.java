package com.marketnest.ecommerce.dto.user.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for updating user profile information")
public class ProfileRequestDto {

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Schema(
            description = "User's first name. Must be between 2 and 50 characters",
            example = "Ahmed",
            minLength = 2,
            maxLength = 50,
            nullable = true
    )
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Schema(
            description = "User's last name. Must be between 2 and 50 characters",
            example = "Ashraf",
            minLength = 2,
            maxLength = 50,
            nullable = true
    )
    private String lastName;

    @Pattern(regexp = "^(\\+\\d{1,3}( )?)?((\\(\\d{1,3}\\))|\\d{1,3})[- .]?\\d{3,4}[- .]?\\d{4}$",
            message = "Invalid phone number format")
    @Schema(
            description = "User's phone number in international format",
            example = "+201234567890",
            pattern = "^(\\+\\d{1,3}( )?)?((\\(\\d{1,3}\\))|\\d{1,3})[- .]?\\d{3,4}[- .]?\\d{4}$",
            nullable = true
    )
    private String phone;
}