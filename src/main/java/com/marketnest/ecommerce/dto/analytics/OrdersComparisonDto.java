package com.marketnest.ecommerce.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for comparing order counts between two time periods")
public class OrdersComparisonDto {

    @Schema(description = "Number of orders in the current period", example = "120")
    private Long currentPeriod;

    @Schema(description = "Number of orders in the previous period for comparison", example = "95")
    private Long previousPeriod;

    @Schema(description = "Percentage change in orders between periods. Positive indicates growth, negative indicates decline",
            example = "26.32")
    private Double percentageChange;
}