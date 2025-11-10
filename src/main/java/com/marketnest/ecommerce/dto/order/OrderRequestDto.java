package com.marketnest.ecommerce.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Data Transfer Object for creating a new order")
public class OrderRequestDto {

    @NotNull(message = "Shipping address ID is required")
    @Schema(
            description = "ID of the address where the order should be shipped",
            example = "5",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long shippingAddressId;

    @Schema(
            description = "ID of the billing address. Uses shipping address if not provided",
            example = "5",
            nullable = true
    )
    private Long billingAddressId;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    @Schema(
            description = "List of items to include in the order. Must contain at least one item",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<OrderItemRequestDto> items;

    @Schema(
            description = "Additional notes or special instructions for the order",
            example = "Please deliver after 5 PM",
            nullable = true
    )
    private String notes;
}