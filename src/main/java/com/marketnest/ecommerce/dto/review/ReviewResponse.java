package com.marketnest.ecommerce.dto.review;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Long userId;
    private String userName;
    private Integer rating;
    private String title;
    private String comment;
    private Boolean verifiedPurchase;
    private Integer helpfulCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isApproved;
}