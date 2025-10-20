package com.marketnest.ecommerce.dto.variant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.marketnest.ecommerce.model.ProductImage;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VariantResponseDto {

    private Long id;
    private Long productId;
    private String sku;
    private String barcode;

    private String option1Name;
    private String option1Value;

    private String option2Name;
    private String option2Value;

    private String option3Name;
    private String option3Value;

    private String title;

    private BigDecimal price;
    private BigDecimal compareAtPrice;

    private Integer stockQuantity;
    private Boolean inStock;

    private BigDecimal weight;

    private List<ProductImage> images;

    private Boolean isAvailable;
    private Boolean onSale;

    private Integer position;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}