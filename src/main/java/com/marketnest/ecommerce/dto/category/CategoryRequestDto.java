package com.marketnest.ecommerce.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "Data Transfer Object for creating or updating product categories")
public class CategoryRequestDto {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    @Schema(
            description = "Name of the category. Must be unique and between 2-100 characters",
            example = "Electronics",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 2,
            maxLength = 100
    )
    private String name;

    @Schema(
            description = "Detailed description of the category and what products it contains",
            example = "All electronic devices including smartphones, laptops, and accessories",
            nullable = true
    )
    private String description;

    @Schema(
            description = "Image file representing the category. Supported formats: JPEG, PNG, WebP",
            type = "string",
            format = "binary",
            nullable = true
    )
    private MultipartFile image;

    @Schema(
            description = "Order in which this category should be displayed. Lower numbers appear first",
            example = "1",
            nullable = true
    )
    private Integer displayOrder;

    @Schema(
            description = "ID of the parent category for creating subcategories. Null for top-level categories",
            example = "10",
            nullable = true
    )
    private Long parentId;
}