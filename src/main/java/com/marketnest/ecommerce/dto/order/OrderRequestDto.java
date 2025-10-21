package com.marketnest.ecommerce.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDto {

    @NotNull(message = "Shipping address ID is required")
    private Long shippingAddressId;

    private Long billingAddressId;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequestDto> items;

    private String notes;
}