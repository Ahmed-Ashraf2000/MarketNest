package com.marketnest.ecommerce.dto.user.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for user profile details in responses")
public class ProfileResponseDto {

    @Schema(description = "Unique identifier of the user", example = "1")
    private Long userId;

    @Schema(description = "User's first name", example = "Ahmed")
    private String firstName;

    @Schema(description = "User's last name", example = "Ashraf")
    private String lastName;

    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @Schema(description = "User's phone number", example = "+201234567890")
    private String phone;

    @Schema(description = "URL to the user's profile photo",
            example = "https://example.com/photos/user.jpg", nullable = true)
    private String photoUrl;

    @Schema(description = "Flag indicating if the user's email has been verified", example = "true")
    private boolean emailVerified;
}