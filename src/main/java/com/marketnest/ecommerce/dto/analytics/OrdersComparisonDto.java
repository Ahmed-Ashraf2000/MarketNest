package com.marketnest.ecommerce.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdersComparisonDto {
    private Long currentPeriod;
    private Long previousPeriod;
    private Double percentageChange;
}