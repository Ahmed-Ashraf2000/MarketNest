package com.marketnest.ecommerce.mapper.order;

import com.marketnest.ecommerce.dto.order.OrderStatusHistoryDto;
import com.marketnest.ecommerce.dto.order.OrderStatusUpdateDto;
import com.marketnest.ecommerce.model.OrderStatusHistory;
import com.marketnest.ecommerce.util.HtmlEscapeUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderStatusHistoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "notes", expression = "java(htmlEscapeUtil.escapeHtml(updateDto.getNotes()))")
    @Mapping(target = "status",
            expression = "java(com.marketnest.ecommerce.model.Order.OrderStatus.valueOf(updateDto.getStatus()))")
    OrderStatusHistory toEntity(OrderStatusUpdateDto updateDto, HtmlEscapeUtil htmlEscapeUtil);

    @Mapping(target = "status", expression = "java(statusHistory.getStatus().name())")
    @Mapping(target = "notes",
            expression = "java(htmlEscapeUtil.escapeHtml(statusHistory.getNotes()))")
    @Mapping(target = "createdBy",
            expression = "java(htmlEscapeUtil.escapeHtml(statusHistory.getCreatedBy()))")
    OrderStatusHistoryDto toDto(OrderStatusHistory statusHistory, HtmlEscapeUtil htmlEscapeUtil);

    default List<OrderStatusHistoryDto> toDtoList(List<OrderStatusHistory> statusHistories,
                                                  HtmlEscapeUtil htmlEscapeUtil) {
        if (statusHistories == null) {
            return null;
        }

        return statusHistories.stream()
                .map(history -> toDto(history, htmlEscapeUtil))
                .toList();
    }
}