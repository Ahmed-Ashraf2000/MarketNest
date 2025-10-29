package com.marketnest.ecommerce.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesStatisticsDto {
    private LocalDate date;
    private Long orderCount;
    private BigDecimal totalSales;
    private BigDecimal averageOrderValue;
}