package com.marketnest.ecommerce.dto.category;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Data Transfer Object for category details in responses. Includes hierarchical structure")
public class CategoryResponseDto {

    @Schema(description = "Unique identifier of the category", example = "1")
    private Long id;

    @Schema(description = "Name of the category", example = "Electronics")
    private String name;

    @Schema(description = "URL-friendly version of the category name", example = "electronics")
    private String slug;

    @Schema(description = "Detailed description of the category",
            example = "All electronic devices including smartphones, laptops, and accessories",
            nullable = true)
    private String description;

    @Schema(description = "URL to the category image",
            example = "https://res.cloudinary.com/demo/image/upload/v1234567890/categories/electronics.jpg",
            nullable = true)
    private String imageUrl;

    @Schema(description = "Flag indicating whether the category is active and visible to customers",
            example = "true")
    private Boolean isActive;

    @Schema(description = "Display order of the category. Lower numbers appear first",
            example = "1", nullable = true)
    private Integer displayOrder;

    @Schema(description = "ID of the parent category if this is a subcategory", example = "10",
            nullable = true)
    private Long parentId;

    @Schema(description = "Name of the parent category if this is a subcategory",
            example = "Home & Garden", nullable = true)
    private String parentName;

    @Schema(description = "List of child/subcategories under this category. Forms hierarchical structure")
    private List<CategoryResponseDto> children = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the category was created",
            example = "2025-01-15T10:30:45")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the category was last updated",
            example = "2025-01-15T14:20:30")
    private LocalDateTime updatedAt;
}