package com.marketnest.ecommerce.mapper.order;

import com.marketnest.ecommerce.dto.order.OrderItemRequestDto;
import com.marketnest.ecommerce.dto.order.OrderItemResponseDto;
import com.marketnest.ecommerce.model.OrderItem;
import com.marketnest.ecommerce.util.HtmlEscapeUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "variant", ignore = true)
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    OrderItem toEntity(OrderItemRequestDto orderItemRequestDto);

    @Mapping(target = "productName",
            expression = "java(htmlEscapeUtil.escapeHtml(orderItem.getProduct().getName()))")
    OrderItemResponseDto toResponse(OrderItem orderItem, HtmlEscapeUtil htmlEscapeUtil);

    default List<OrderItemResponseDto> toResponseList(List<OrderItem> items,
                                                      HtmlEscapeUtil htmlEscapeUtil) {
        if (items == null) {
            return null;
        }

        return items.stream()
                .map(item -> toResponse(item, htmlEscapeUtil))
                .toList();
    }
}