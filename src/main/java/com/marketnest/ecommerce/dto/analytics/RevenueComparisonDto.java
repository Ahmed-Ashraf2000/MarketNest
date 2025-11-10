package com.marketnest.ecommerce.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for comparing revenue between two time periods")
public class RevenueComparisonDto {

    @Schema(description = "Revenue generated in the current period", example = "45000.75")
    private BigDecimal currentPeriod;

    @Schema(description = "Revenue generated in the previous period for comparison",
            example = "38500.50")
    private BigDecimal previousPeriod;

    @Schema(description = "Percentage change in revenue between periods. Positive indicates growth, negative indicates decline",
            example = "16.88")
    private BigDecimal percentageChange;
}