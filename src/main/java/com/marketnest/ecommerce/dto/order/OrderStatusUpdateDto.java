package com.marketnest.ecommerce.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusUpdateDto {

    @NotNull(message = "Status is required")
    @NotBlank(message = "Status cannot be blank")
    private String status;

    private String notes;
}