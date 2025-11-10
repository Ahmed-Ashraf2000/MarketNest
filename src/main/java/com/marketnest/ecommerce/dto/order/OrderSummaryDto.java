package com.marketnest.ecommerce.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Data Transfer Object for order summary in list views. Contains essential order information without full details")
public class OrderSummaryDto {

    @Schema(description = "Unique identifier of the order", example = "1")
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Date and time when the order was placed",
            example = "2025-01-15T10:30:45")
    private LocalDateTime orderDate;

    @Schema(
            description = "Current status of the order",
            example = "PROCESSING",
            allowableValues = {"PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED",
                    "RETURNED"}
    )
    private String status;

    @Schema(description = "Final total amount of the order", example = "618.98")
    private BigDecimal total;

    @Schema(description = "Total number of items in the order", example = "3")
    private int itemCount;

    @Schema(description = "Tracking number for shipment if available",
            example = "1Z999AA10123456784", nullable = true)
    private String trackingNumber;
}