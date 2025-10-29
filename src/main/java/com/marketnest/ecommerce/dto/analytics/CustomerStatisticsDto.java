package com.marketnest.ecommerce.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatisticsDto {
    private Long totalCustomers;
    private Long newCustomersThisMonth;
    private Long activeCustomers;
    private Double customerRetentionRate;
    private Long customersWithOrders;
}