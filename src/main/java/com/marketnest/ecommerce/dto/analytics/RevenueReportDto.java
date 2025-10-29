package com.marketnest.ecommerce.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportDto {
    private LocalDate date;
    private BigDecimal revenue;
    private BigDecimal profit;
    private Long orderCount;
}