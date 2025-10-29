package com.marketnest.ecommerce.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewDto {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalCustomers;
    private Long totalProducts;
    private BigDecimal averageOrderValue;
    private Long pendingOrders;
    private Long lowStockProducts;
    private com.marketnest.ecommerce.dto.analytics.RevenueComparisonDto revenueComparison;
    private OrdersComparisonDto ordersComparison;
}