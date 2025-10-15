package com.marketnest.ecommerce.dto.variant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.marketnest.ecommerce.model.ProductImage;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageResponseDto {

    private Long id;
    private Long productId;
    private Long variantId;

    private String url;
    private String altText;
    private String title;

    private String fileName;
    private Long fileSize;
    private String mimeType;
    private Integer width;
    private Integer height;

    private ProductImage.ImageType imageType;
    private Integer position;
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}