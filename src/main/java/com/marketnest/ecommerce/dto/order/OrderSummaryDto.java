package com.marketnest.ecommerce.dto.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderSummaryDto {

    private Long id;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal total;
    private int itemCount;
    private String trackingNumber;
}