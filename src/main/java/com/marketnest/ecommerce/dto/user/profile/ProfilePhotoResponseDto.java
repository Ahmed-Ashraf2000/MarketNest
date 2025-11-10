package com.marketnest.ecommerce.dto.user.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for profile photo upload/update responses")
public class ProfilePhotoResponseDto {

    @Schema(
            description = "URL of the user's profile photo. Hosted on Cloudinary CDN",
            example = "https://res.cloudinary.com/demo/image/upload/v1234567890/profiles/user_123.jpg"
    )
    private String photoUrl;
}