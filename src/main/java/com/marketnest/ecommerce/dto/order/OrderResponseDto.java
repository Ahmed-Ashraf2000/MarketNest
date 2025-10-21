package com.marketnest.ecommerce.dto.order;

import com.marketnest.ecommerce.dto.payment.PaymentResponseDto;
import com.marketnest.ecommerce.dto.user.address.AddressResponseDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDto {

    private Long id;
    private LocalDateTime orderDate;
    private String status;
    private AddressResponseDTO shippingAddress;
    private AddressResponseDTO billingAddress;
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal total;
    private String notes;
    private String trackingNumber;
    private List<OrderItemResponseDto> items;
    private List<OrderStatusHistoryDto> statusHistory;
    private List<PaymentResponseDto> payments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}