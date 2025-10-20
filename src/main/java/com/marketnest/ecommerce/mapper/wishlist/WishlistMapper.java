package com.marketnest.ecommerce.mapper.wishlist;

import com.marketnest.ecommerce.dto.wishlist.AddWishlistItemRequest;
import com.marketnest.ecommerce.dto.wishlist.UpdateWishlistItemRequest;
import com.marketnest.ecommerce.dto.wishlist.WishlistItemResponse;
import com.marketnest.ecommerce.dto.wishlist.WishlistResponse;
import com.marketnest.ecommerce.model.Wishlist;
import com.marketnest.ecommerce.model.WishlistItem;
import com.marketnest.ecommerce.util.HtmlEscapeUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface WishlistMapper {

    @Mapping(target = "totalItems", expression = "java(wishlist.getTotalItems())")
    @Mapping(target = "items",
            expression = "java(toResponseList(wishlist.getWishlistItems(), htmlEscapeUtil))")
    WishlistResponse toResponse(Wishlist wishlist, HtmlEscapeUtil htmlEscapeUtil);

    @Mapping(target = "productId", expression = "java(wishlistItem.getProduct().getId())")
    @Mapping(target = "productName",
            expression = "java(htmlEscapeUtil.escapeHtml(wishlistItem.getProduct().getName()))")
    @Mapping(target = "productSku",
            expression = "java(htmlEscapeUtil.escapeHtml(wishlistItem.getProduct().getSku()))")
    @Mapping(target = "productPrice", expression = "java(wishlistItem.getProduct().getPrice())")
    @Mapping(target = "notes",
            expression = "java(htmlEscapeUtil.escapeHtml(wishlistItem.getNotes()))")
    WishlistItemResponse toResponse(WishlistItem wishlistItem, HtmlEscapeUtil htmlEscapeUtil);

    default List<WishlistItemResponse> toResponseList(List<WishlistItem> wishlistItems,
                                                      HtmlEscapeUtil htmlEscapeUtil) {
        if (wishlistItems == null) {
            return null;
        }

        List<WishlistItemResponse> list = new ArrayList<>(wishlistItems.size());
        for (WishlistItem item : wishlistItems) {
            list.add(toResponse(item, htmlEscapeUtil));
        }
        return list;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "wishlist", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "addedAt", ignore = true)
    WishlistItem toEntity(AddWishlistItemRequest request);

    void updateEntity(@MappingTarget WishlistItem wishlistItem, UpdateWishlistItemRequest request);
}