package com.marketnest.ecommerce.dto.analytics;

import com.marketnest.ecommerce.model.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for order status distribution statistics")
public class OrderStatusStatisticsDto {

    @Schema(
            description = "Order status category",
            example = "DELIVERED",
            allowableValues = {"PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"}
    )
    private Order.OrderStatus status;

    @Schema(description = "Number of orders in this status", example = "85")
    private Long count;

    @Schema(description = "Percentage of total orders in this status", example = "68.5")
    private Double percentage;
}