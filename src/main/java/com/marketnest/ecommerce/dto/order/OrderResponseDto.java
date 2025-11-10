package com.marketnest.ecommerce.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.marketnest.ecommerce.dto.payment.PaymentResponseDto;
import com.marketnest.ecommerce.dto.user.address.AddressResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Data Transfer Object for complete order details in responses. Includes items, addresses, payments, and status history")
public class OrderResponseDto {

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

    @Schema(description = "Complete shipping address details")
    private AddressResponseDTO shippingAddress;

    @Schema(description = "Complete billing address details", nullable = true)
    private AddressResponseDTO billingAddress;

    @Schema(description = "Sum of all item prices before shipping, tax, and discounts",
            example = "599.98")
    private BigDecimal subtotal;

    @Schema(description = "Shipping cost for this order", example = "15.00")
    private BigDecimal shippingCost;

    @Schema(description = "Tax amount calculated for this order", example = "54.00")
    private BigDecimal tax;

    @Schema(description = "Total discount applied to this order (from coupons, promotions, etc.)",
            example = "50.00")
    private BigDecimal discount;

    @Schema(description = "Final total amount to be paid (subtotal + shipping + tax - discount)",
            example = "618.98")
    private BigDecimal total;

    @Schema(description = "Additional notes or special instructions for the order",
            example = "Please deliver after 5 PM", nullable = true)
    private String notes;

    @Schema(description = "Tracking number for shipment. Available once order is shipped",
            example = "1Z999AA10123456784", nullable = true)
    private String trackingNumber;

    @Schema(description = "List of all items included in this order")
    private List<OrderItemResponseDto> items;

    @Schema(description = "Complete history of status changes for this order with timestamps and notes")
    private List<OrderStatusHistoryDto> statusHistory;

    @Schema(description = "List of all payment transactions associated with this order")
    private List<PaymentResponseDto> payments;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the order was created", example = "2025-01-15T10:30:45")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the order was last updated",
            example = "2025-01-15T14:20:30")
    private LocalDateTime updatedAt;
}