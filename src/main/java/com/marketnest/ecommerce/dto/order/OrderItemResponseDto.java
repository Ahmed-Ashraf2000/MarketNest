package com.marketnest.ecommerce.dto.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponseDto {

    private Long id;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}

