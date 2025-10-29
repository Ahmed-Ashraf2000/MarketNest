package com.marketnest.ecommerce.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LowStockProductDto {
    private Long productId;
    private String productName;
    private String sku;
    private Integer currentStock;
    private Integer lowStockThreshold;
    private String imageUrl;
}