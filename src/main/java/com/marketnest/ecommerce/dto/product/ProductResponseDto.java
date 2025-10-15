package com.marketnest.ecommerce.dto.product;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.marketnest.ecommerce.model.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {

    private Long id;
    private String sku;
    private String name;
    private String slug;
    private String description;

    private BigDecimal price;
    private BigDecimal compareAtPrice;

    private Integer stockQuantity;
    private Integer lowStockThreshold;

    private Long categoryId;
    private String categoryName;

    private String brand;
    private BigDecimal weight;
    private String dimensions;

    private List<ProductImage> images;

    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;

    private Boolean isActive;
    private Boolean isFeatured;
    private Boolean isTaxable;

    private Boolean inStock;
    private Boolean lowStock;
    private Boolean onSale;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
