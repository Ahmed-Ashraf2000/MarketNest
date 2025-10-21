package com.marketnest.ecommerce.dto.order;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderStatusHistoryDto {

    private Long id;
    private String status;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;
}