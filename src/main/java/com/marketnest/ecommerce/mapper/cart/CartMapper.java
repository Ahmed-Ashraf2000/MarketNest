package com.marketnest.ecommerce.mapper.cart;

import com.marketnest.ecommerce.dto.cart.CartItemRequest;
import com.marketnest.ecommerce.dto.cart.CartItemResponse;
import com.marketnest.ecommerce.dto.cart.CartResponse;
import com.marketnest.ecommerce.model.Cart;
import com.marketnest.ecommerce.model.CartItem;
import com.marketnest.ecommerce.util.HtmlEscapeUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "status", expression = "java(cart.getStatus().name())")
    @Mapping(target = "totalItems", expression = "java(cart.getCartItems().size())")
    @Mapping(target = "items",
            expression = "java(toResponseList(cart.getCartItems(), htmlEscapeUtil))")
    CartResponse toResponse(Cart cart, HtmlEscapeUtil htmlEscapeUtil);

    @Mapping(target = "productId", expression = "java(cartItem.getProduct().getId())")
    @Mapping(target = "productName",
            expression = "java(htmlEscapeUtil.escapeHtml(cartItem.getProduct().getName()))")
    @Mapping(target = "productSku",
            expression = "java(htmlEscapeUtil.escapeHtml(cartItem.getProduct().getSku()))")
    @Mapping(target = "addedAt", expression = "java(cartItem.getCreatedAt())")
    @Mapping(target = "subtotal", expression = "java(cartItem.getSubtotal())")
    CartItemResponse toResponse(CartItem cartItem, HtmlEscapeUtil htmlEscapeUtil);

    default List<CartItemResponse> toResponseList(List<CartItem> cartItems,
                                                  HtmlEscapeUtil htmlEscapeUtil) {
        if (cartItems == null) {
            return null;
        }

        List<CartItemResponse> list = new ArrayList<>(cartItems.size());
        for (CartItem cartItem : cartItems) {
            list.add(toResponse(cartItem, htmlEscapeUtil));
        }
        return list;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CartItem toEntity(CartItemRequest request);
}