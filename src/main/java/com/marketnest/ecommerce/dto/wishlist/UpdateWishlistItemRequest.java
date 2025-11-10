package com.marketnest.ecommerce.dto.wishlist;

import com.marketnest.ecommerce.model.WishlistItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for updating an existing wishlist item")
public class UpdateWishlistItemRequest {

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(
            description = "Updated notes about this wishlist item",
            example = "Changed preference - now wants red color instead",
            maxLength = 500,
            nullable = true
    )
    private String notes;

    @Schema(
            description = "Updated priority level for this wishlist item",
            example = "HIGH",
            allowableValues = {"LOW", "MEDIUM", "HIGH"},
            nullable = true
    )
    private WishlistItem.Priority priority;

    @Schema(
            description = "Desired quantity of this item. Useful for planning bulk purchases",
            example = "2",
            minimum = "1",
            nullable = true
    )
    private Integer quantity;
}