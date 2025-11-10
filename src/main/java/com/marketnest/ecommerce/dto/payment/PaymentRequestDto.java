package com.marketnest.ecommerce.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Data Transfer Object for creating a payment record. Used for manual payment entry or verification")
public class PaymentRequestDto {

    @NotNull(message = "Order ID is required")
    @Schema(
            description = "ID of the order this payment is for",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long orderId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(
            description = "Payment amount. Must be positive and match the order total or be a partial payment",
            example = "618.98",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0.01"
    )
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    @NotBlank(message = "Payment method cannot be blank")
    @Schema(
            description = "Payment method used for this transaction",
            example = "stripe",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"stripe", "paypal", "cod"}
    )
    private String paymentMethod;

    @Schema(
            description = "External transaction ID from payment gateway (e.g., Stripe charge ID, PayPal transaction ID)",
            example = "ch_3L8xYz2eZvKYlo2C0X9n4gFH",
            nullable = true
    )
    private String transactionId;
}