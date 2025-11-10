package com.marketnest.ecommerce.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for available payment methods configuration")
public class PaymentMethodDto {

    @Schema(
            description = "Unique code identifier for the payment method",
            example = "stripe",
            allowableValues = {"stripe", "paypal", "cod"}
    )
    private String code;

    @Schema(
            description = "Display name of the payment method",
            example = "Credit/Debit Card"
    )
    private String name;

    @Schema(
            description = "Detailed description of the payment method and its features",
            example = "Pay securely using your credit or debit card via Stripe"
    )
    private String description;

    @Schema(
            description = "Flag indicating if this payment method is currently enabled and available for use",
            example = "true"
    )
    private boolean enabled;
}