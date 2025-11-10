package com.marketnest.ecommerce.dto.wishlist;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Data Transfer Object for complete wishlist details in responses. Contains all wishlist items for a user")
public class WishlistResponse {

    @Schema(description = "Unique identifier of the wishlist", example = "1")
    private Long id;

    @Schema(description = "ID of the user who owns this wishlist", example = "5")
    private Long userId;

    @Schema(description = "List of all items in this wishlist with complete product details")
    private List<WishlistItemResponse> items;

    @Schema(description = "Total number of items in the wishlist", example = "5")
    private Integer totalItems;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the wishlist was created",
            example = "2025-01-10T08:15:30")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the wishlist was last updated",
            example = "2025-01-15T14:20:30")
    private LocalDateTime updatedAt;
}