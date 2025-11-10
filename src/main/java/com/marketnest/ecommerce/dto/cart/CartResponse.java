package com.marketnest.ecommerce.dto.cart;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Data Transfer Object for complete shopping cart details in responses")
public class CartResponse {

    @Schema(description = "Unique identifier of the shopping cart", example = "1")
    private Long id;

    @Schema(description = "ID of the user who owns this cart", example = "5")
    private Long userId;

    @Schema(
            description = "Current status of the cart",
            example = "ACTIVE",
            allowableValues = {"ACTIVE", "ABANDONED", "COMPLETED", "MERGED"}
    )
    private String status;

    @Schema(description = "List of all items in the cart with complete product details")
    private List<CartItemResponse> items;

    @Schema(description = "Total number of items in the cart", example = "3")
    private Integer totalItems;

    @Schema(description = "Total price of all items in the cart (sum of all subtotals)",
            example = "899.97")
    private BigDecimal totalPrice;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the cart was created", example = "2025-01-10T08:15:30")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the cart was last updated",
            example = "2025-01-15T14:20:30")
    private LocalDateTime updatedAt;
}