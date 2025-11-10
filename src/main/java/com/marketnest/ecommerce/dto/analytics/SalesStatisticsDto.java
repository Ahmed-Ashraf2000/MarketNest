package com.marketnest.ecommerce.dto.analytics;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for daily sales statistics and trends")
public class SalesStatisticsDto {

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Date of the sales statistics", example = "2024-01-15")
    private LocalDate date;

    @Schema(description = "Number of orders placed on this date", example = "18")
    private Long orderCount;

    @Schema(description = "Total sales amount on this date", example = "5250.75")
    private BigDecimal totalSales;

    @Schema(description = "Average order value on this date (total sales / order count)",
            example = "291.71")
    private BigDecimal averageOrderValue;
}