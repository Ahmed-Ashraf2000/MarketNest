package com.marketnest.ecommerce.dto.wishlist;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WishlistResponse {
    private Long id;
    private Long userId;
    private List<WishlistItemResponse> items;
    private Integer totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
