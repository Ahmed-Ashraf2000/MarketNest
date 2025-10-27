package com.marketnest.ecommerce.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentProcessRequestDto {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Payment method is required")
    @NotBlank(message = "Payment method cannot be blank")
    private String paymentMethod;

    private String stripeToken;

    private String stripePaymentMethodId;

    private String email;
}