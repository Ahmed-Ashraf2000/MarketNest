package com.marketnest.ecommerce.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Data Transfer Object for requesting a refund on a payment. Used for full or partial refunds")
public class RefundRequestDto {

    @Positive(message = "Amount must be positive")
    @Schema(
            description = "Refund amount. Must be positive and not exceed the original payment amount. Leave null for full refund",
            example = "299.99",
            minimum = "0.01",
            nullable = true
    )
    private BigDecimal amount;

    @NotBlank(message = "Reason is required")
    @Schema(
            description = "Reason for the refund. Used for record keeping and customer communication",
            example = "Customer returned the product - defective item",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String reason;
}