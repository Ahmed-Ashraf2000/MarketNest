package com.marketnest.ecommerce.dto.wishlist;

import com.marketnest.ecommerce.model.WishlistItem;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateWishlistItemRequest {
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    private WishlistItem.Priority priority;

    private Integer quantity;
}