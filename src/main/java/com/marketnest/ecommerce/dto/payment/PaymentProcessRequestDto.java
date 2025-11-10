package com.marketnest.ecommerce.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for processing a payment for an order. Used in checkout flow")
public class PaymentProcessRequestDto {

    @NotNull(message = "Order ID is required")
    @Schema(
            description = "ID of the order being paid for",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long orderId;

    @NotNull(message = "Payment method is required")
    @NotBlank(message = "Payment method cannot be blank")
    @Schema(
            description = "Payment method code to use for processing",
            example = "stripe",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"stripe", "paypal", "cod"}
    )
    private String paymentMethod;

    @Schema(
            description = "Stripe token for card payments. Required when using Stripe with tokenization",
            example = "tok_visa",
            nullable = true
    )
    private String stripeToken;

    @Schema(
            description = "Stripe Payment Method ID for saved cards or new payment methods. Used with Stripe Payment Intents API",
            example = "pm_1234567890abcdef",
            nullable = true
    )
    private String stripePaymentMethodId;

    @Schema(
            description = "Customer email address for payment receipt and notifications",
            example = "customer@example.com",
            nullable = true
    )
    private String email;
}