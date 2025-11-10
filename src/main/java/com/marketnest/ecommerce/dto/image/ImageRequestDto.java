package com.marketnest.ecommerce.dto.image;

import com.marketnest.ecommerce.model.ProductImage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "Data Transfer Object for uploading product or variant images")
public class ImageRequestDto {

    @NotNull(message = "Image file is required")
    @Schema(
            description = "Image file to upload. Supported formats: JPEG, PNG, WebP",
            requiredMode = Schema.RequiredMode.REQUIRED,
            type = "string",
            format = "binary"
    )
    private MultipartFile file;

    @Size(max = 255, message = "Alt text must not exceed 255 characters")
    @Schema(
            description = "Alternative text for the image. Used for accessibility and SEO",
            example = "Red running shoes side view",
            maxLength = 255,
            nullable = true
    )
    private String altText;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(
            description = "Title of the image. Displayed on hover",
            example = "Premium Running Shoes",
            maxLength = 255,
            nullable = true
    )
    private String title;

    @Schema(
            description = "Type of image in the product gallery",
            example = "GALLERY",
            allowableValues = {"PRIMARY", "GALLERY", "THUMBNAIL", "VARIANT_SPECIFIC"},
            nullable = true
    )
    private ProductImage.ImageType imageType;

    @Min(value = 0, message = "Position cannot be negative")
    @Schema(
            description = "Display position/order of the image in the gallery. Lower numbers appear first",
            example = "1",
            minimum = "0",
            nullable = true
    )
    private Integer position;

    @Schema(
            description = "Flag indicating whether the image is active and should be displayed",
            example = "true",
            nullable = true
    )
    private Boolean isActive;
}