package com.marketnest.ecommerce.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueComparisonDto {
    private BigDecimal currentPeriod;
    private BigDecimal previousPeriod;
    private BigDecimal percentageChange;
}