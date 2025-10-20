package com.marketnest.ecommerce.dto.wishlist;

import com.marketnest.ecommerce.model.WishlistItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WishlistItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal productPrice;
    private Integer quantity;
    private String notes;
    private WishlistItem.Priority priority;
    private LocalDateTime addedAt;
}