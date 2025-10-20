package com.marketnest.ecommerce.dto.wishlist;

import com.marketnest.ecommerce.model.WishlistItem;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddWishlistItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    private WishlistItem.Priority priority = WishlistItem.Priority.MEDIUM;
}