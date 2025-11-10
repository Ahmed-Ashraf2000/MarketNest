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
@Schema(description = "Data Transfer Object for daily revenue reports with profit metrics")
public class RevenueReportDto {

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Date of the revenue report", example = "2025-01-15")
    private LocalDate date;

    @Schema(description = "Total revenue generated on this date from all completed orders",
            example = "5250.75")
    private BigDecimal revenue;

    @Schema(description = "Profit calculated as revenue minus costs (COGS, shipping, etc.)",
            example = "1575.25")
    private BigDecimal profit;

    @Schema(description = "Number of orders completed on this date", example = "18")
    private Long orderCount;
}