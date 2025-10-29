package com.marketnest.ecommerce.dto.analytics;

import com.marketnest.ecommerce.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusStatisticsDto {
    private Order.OrderStatus status;
    private Long count;
    private Double percentage;
}