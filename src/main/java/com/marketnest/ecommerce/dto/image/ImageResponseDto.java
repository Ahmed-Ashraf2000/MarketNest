package com.marketnest.ecommerce.dto.image;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.marketnest.ecommerce.model.ProductImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Data Transfer Object for product or variant image details in responses")
public class ImageResponseDto {

    @Schema(description = "Unique identifier of the image", example = "1")
    private Long id;

    @Schema(description = "ID of the product this image belongs to", example = "101",
            nullable = true)
    private Long productId;

    @Schema(description = "ID of the variant this image belongs to (if variant-specific)",
            example = "501", nullable = true)
    private Long variantId;

    @Schema(description = "URL to access the uploaded image",
            example = "https://res.cloudinary.com/demo/image/upload/v1234567890/products/image.jpg")
    private String url;

    @Schema(description = "Alternative text for the image. Used for accessibility and SEO",
            example = "Red running shoes side view", nullable = true)
    private String altText;

    @Schema(description = "Title of the image", example = "Premium Running Shoes", nullable = true)
    private String title;

    @Schema(description = "Original file name of the uploaded image", example = "running-shoes.jpg",
            nullable = true)
    private String fileName;

    @Schema(description = "Size of the image file in bytes", example = "245760", nullable = true)
    private Long fileSize;

    @Schema(description = "MIME type of the image", example = "image/jpeg", nullable = true)
    private String mimeType;

    @Schema(description = "Width of the image in pixels", example = "1920", nullable = true)
    private Integer width;

    @Schema(description = "Height of the image in pixels", example = "1080", nullable = true)
    private Integer height;

    @Schema(
            description = "Type of image in the product gallery",
            example = "GALLERY",
            allowableValues = {"PRIMARY", "GALLERY", "THUMBNAIL", "VARIANT_SPECIFIC"},
            nullable = true
    )
    private ProductImage.ImageType imageType;

    @Schema(description = "Display position/order of the image in the gallery", example = "1",
            nullable = true)
    private Integer position;

    @Schema(description = "Flag indicating whether the image is active and should be displayed",
            example = "true", nullable = true)
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the image was uploaded", example = "2025-01-15T10:30:45",
            nullable = true)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the image was last updated",
            example = "2025-01-15T14:20:30", nullable = true)
    private LocalDateTime updatedAt;
}