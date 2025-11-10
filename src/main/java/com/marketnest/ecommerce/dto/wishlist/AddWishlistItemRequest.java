package com.marketnest.ecommerce.dto.wishlist;

import com.marketnest.ecommerce.model.WishlistItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for adding a product to user's wishlist")
public class AddWishlistItemRequest {

    @NotNull(message = "Product ID is required")
    @Schema(
            description = "ID of the product to add to wishlist",
            example = "101",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long productId;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(
            description = "Optional notes about why this product is in the wishlist or specific preferences",
            example = "Birthday gift for mom - prefers blue color",
            maxLength = 500,
            nullable = true
    )
    private String notes;

    @Schema(
            description = "Priority level for this wishlist item. Helps users organize their wishlist",
            example = "HIGH",
            defaultValue = "MEDIUM",
            allowableValues = {"LOW", "MEDIUM", "HIGH"}
    )
    private WishlistItem.Priority priority = WishlistItem.Priority.MEDIUM;
}