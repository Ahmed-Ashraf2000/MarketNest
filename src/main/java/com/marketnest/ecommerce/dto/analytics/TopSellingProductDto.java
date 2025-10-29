package com.marketnest.ecommerce.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingProductDto {
    private Long productId;
    private String productName;
    private String sku;
    private Long totalQuantitySold;
    private BigDecimal totalRevenue;
    private String imageUrl;
}