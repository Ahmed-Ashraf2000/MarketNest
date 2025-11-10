package com.marketnest.ecommerce.dto.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Data Transfer Object for payment transaction details in responses")
public class PaymentResponseDto {

    @Schema(description = "Unique identifier of the payment record", example = "1")
    private Long id;

    @Schema(description = "Payment amount processed", example = "618.98")
    private BigDecimal amount;

    @Schema(
            description = "Payment method used for this transaction",
            example = "stripe",
            allowableValues = {"stripe", "paypal", "cod"}
    )
    private String paymentMethod;

    @Schema(
            description = "Current status of the payment",
            example = "COMPLETED",
            allowableValues = {"PENDING", "PROCESSING", "COMPLETED", "FAILED", "REFUNDED",
                    "PARTIALLY_REFUNDED"}
    )
    private String status;

    @Schema(
            description = "External transaction ID from payment gateway",
            example = "ch_3L8xYz2eZvKYlo2C0X9n4gFH",
            nullable = true
    )
    private String transactionId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(
            description = "Date and time when the payment was successfully processed",
            example = "2025-01-15T10:35:20"
    )
    private LocalDateTime paymentDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the payment record was created",
            example = "2025-01-15T10:30:45")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the payment record was last updated",
            example = "2025-01-15T10:35:20")
    private LocalDateTime updatedAt;
}