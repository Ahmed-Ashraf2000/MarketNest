package com.marketnest.ecommerce.mapper.order;

import com.marketnest.ecommerce.dto.order.OrderRequestDto;
import com.marketnest.ecommerce.dto.order.OrderResponseDto;
import com.marketnest.ecommerce.dto.order.OrderStatusUpdateDto;
import com.marketnest.ecommerce.dto.order.OrderSummaryDto;
import com.marketnest.ecommerce.mapper.payment.PaymentMapper;
import com.marketnest.ecommerce.mapper.user.UserAddressMapper;
import com.marketnest.ecommerce.model.Order;
import com.marketnest.ecommerce.util.HtmlEscapeUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {OrderItemMapper.class, OrderStatusHistoryMapper.class, PaymentMapper.class})
public abstract class OrderMapper {

    @Autowired
    protected OrderItemMapper orderItemMapper;

    @Autowired
    protected UserAddressMapper userAddressMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "shippingAddress", ignore = true)
    @Mapping(target = "billingAddress", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "shippingCost", ignore = true)
    @Mapping(target = "tax", ignore = true)
    @Mapping(target = "discount", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "trackingNumber", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "statusHistory", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "notes",
            expression = "java(htmlEscapeUtil.escapeHtml(orderRequestDto.getNotes()))")
    public abstract Order toEntity(OrderRequestDto orderRequestDto, HtmlEscapeUtil htmlEscapeUtil);

    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    @Mapping(target = "notes", expression = "java(htmlEscapeUtil.escapeHtml(order.getNotes()))")
    @Mapping(target = "trackingNumber",
            expression = "java(htmlEscapeUtil.escapeHtml(order.getTrackingNumber()))")
    @Mapping(target = "items",
            expression = "java(orderItemMapper.toResponseList(order.getOrderItems(), htmlEscapeUtil))")
    @Mapping(target = "shippingAddress",
            expression = "java(userAddressMapper.toResponseDTO(order.getShippingAddress()))")
    @Mapping(target = "billingAddress",
            expression = "java(userAddressMapper.toResponseDTO(order.getBillingAddress()))")
    public abstract OrderResponseDto toResponse(Order order, HtmlEscapeUtil htmlEscapeUtil);

    @Mapping(target = "itemCount", expression = "java(order.getOrderItems().size())")
    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    @Mapping(target = "trackingNumber",
            expression = "java(htmlEscapeUtil.escapeHtml(order.getTrackingNumber()))")
    public abstract OrderSummaryDto toSummary(Order order, HtmlEscapeUtil htmlEscapeUtil);

    public List<OrderSummaryDto> toSummaryList(List<Order> orders, HtmlEscapeUtil htmlEscapeUtil) {
        if (orders == null) {
            return null;
        }

        return orders.stream()
                .map(order -> toSummary(order, htmlEscapeUtil))
                .toList();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "shippingAddress", ignore = true)
    @Mapping(target = "billingAddress", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "shippingCost", ignore = true)
    @Mapping(target = "tax", ignore = true)
    @Mapping(target = "discount", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "statusHistory", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "notes",
            expression = "java(htmlEscapeUtil.escapeHtml(statusUpdateDto.getNotes()))")
    @Mapping(target = "status",
            expression = "java(Order.OrderStatus.valueOf(statusUpdateDto.getStatus()))")
    public abstract void updateOrderStatus(@MappingTarget Order order,
                                           OrderStatusUpdateDto statusUpdateDto,
                                           HtmlEscapeUtil htmlEscapeUtil);
}