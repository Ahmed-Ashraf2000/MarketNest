package com.marketnest.ecommerce.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for comprehensive dashboard overview with key business metrics")
public class DashboardOverviewDto {

    @Schema(description = "Total revenue generated from all completed orders",
            example = "125000.50")
    private BigDecimal totalRevenue;

    @Schema(description = "Total number of orders placed in the system", example = "450")
    private Long totalOrders;

    @Schema(description = "Total number of registered customers", example = "1250")
    private Long totalCustomers;

    @Schema(description = "Total number of products available in the catalog", example = "350")
    private Long totalProducts;

    @Schema(description = "Average order value calculated as total revenue divided by total orders",
            example = "277.78")
    private BigDecimal averageOrderValue;

    @Schema(description = "Number of orders currently in pending status awaiting processing",
            example = "12")
    private Long pendingOrders;

    @Schema(description = "Number of products with stock levels below their low stock threshold",
            example = "8")
    private Long lowStockProducts;

    @Schema(description = "Revenue comparison between current and previous period with percentage change")
    private RevenueComparisonDto revenueComparison;

    @Schema(description = "Orders count comparison between current and previous period with percentage change")
    private OrdersComparisonDto ordersComparison;
}