package com.marketnest.ecommerce.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for updating order status (admin operation)")
public class OrderStatusUpdateDto {

    @NotNull(message = "Status is required")
    @NotBlank(message = "Status cannot be blank")
    @Schema(
            description = "New status to set for the order",
            example = "SHIPPED",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED",
                    "RETURNED"}
    )
    private String status;

    @Schema(
            description = "Optional notes explaining the status change",
            example = "Order shipped via Aramex, expected delivery in 2-3 business days",
            nullable = true
    )
    private String notes;
}