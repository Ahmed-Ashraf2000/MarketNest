package com.marketnest.ecommerce.dto;

import com.marketnest.ecommerce.model.ProductImage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageRequestDto {

    @NotNull(message = "Image file is required")
    private MultipartFile file;

    @Size(max = 255, message = "Alt text must not exceed 255 characters")
    private String altText;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private ProductImage.ImageType imageType;

    @Min(value = 0, message = "Position cannot be negative")
    private Integer position;

    private Boolean isActive;
}