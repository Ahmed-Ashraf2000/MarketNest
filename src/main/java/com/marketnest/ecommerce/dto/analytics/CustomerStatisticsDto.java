package com.marketnest.ecommerce.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for customer statistics and metrics in analytics dashboard")
public class CustomerStatisticsDto {

    @Schema(description = "Total number of registered customers in the system", example = "1250")
    private Long totalCustomers;

    @Schema(description = "Number of new customers registered in the current month", example = "45")
    private Long newCustomersThisMonth;

    @Schema(description = "Number of customers who have placed at least one order recently",
            example = "850")
    private Long activeCustomers;

    @Schema(description = "Percentage of customers who made repeat purchases. Calculated as (returning customers / total customers) × 100",
            example = "68.5")
    private Double customerRetentionRate;

    @Schema(description = "Number of customers who have placed at least one order", example = "980")
    private Long customersWithOrders;
}