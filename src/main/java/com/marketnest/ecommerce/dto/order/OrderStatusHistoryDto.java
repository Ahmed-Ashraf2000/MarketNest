package com.marketnest.ecommerce.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Data Transfer Object for tracking order status changes and history")
public class OrderStatusHistoryDto {

    @Schema(description = "Unique identifier of the status history entry", example = "1")
    private Long id;

    @Schema(
            description = "Status at this point in history",
            example = "SHIPPED",
            allowableValues = {"PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED",
                    "RETURNED"}
    )
    private String status;

    @Schema(description = "Additional notes or comments about this status change",
            example = "Order dispatched from warehouse", nullable = true)
    private String notes;

    @Schema(description = "Username or identifier of the person who made this status change",
            example = "admin@marketnest.com", nullable = true)
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when this status change occurred",
            example = "2025-01-16T09:15:30")
    private LocalDateTime createdAt;
}