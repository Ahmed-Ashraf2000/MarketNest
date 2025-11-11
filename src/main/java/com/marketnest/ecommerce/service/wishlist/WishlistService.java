package com.marketnest.ecommerce.service.wishlist;

import com.marketnest.ecommerce.dto.cart.CartItemRequest;
import com.marketnest.ecommerce.dto.cart.CartResponse;
import com.marketnest.ecommerce.dto.wishlist.AddWishlistItemRequest;
import com.marketnest.ecommerce.dto.wishlist.WishlistResponse;
import com.marketnest.ecommerce.exception.ProductNotFoundException;
import com.marketnest.ecommerce.exception.WishlistNotFoundException;
import com.marketnest.ecommerce.mapper.wishlist.WishlistMapper;
import com.marketnest.ecommerce.model.Product;
import com.marketnest.ecommerce.model.Wishlist;
import com.marketnest.ecommerce.model.WishlistItem;
import com.marketnest.ecommerce.repository.ProductRepository;
import com.marketnest.ecommerce.repository.WishlistItemRepository;
import com.marketnest.ecommerce.repository.WishlistRepository;
import com.marketnest.ecommerce.service.cart.CartService;
import com.marketnest.ecommerce.util.HtmlEscapeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final WishlistMapper wishlistMapper;
    private final CartService cartService;
    private final HtmlEscapeUtil htmlEscapeUtil;


    @Transactional
    public WishlistResponse getUserWishlist(Long userId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        return wishlistMapper.toResponse(wishlist, htmlEscapeUtil);
    }


    @Transactional
    public WishlistResponse addToWishlist(Long userId, AddWishlistItemRequest request) {
        Wishlist wishlist = getOrCreateWishlist(userId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (wishlist.containsProduct(product.getId())) {
            throw new IllegalArgumentException("Product already in wishlist");
        }

        WishlistItem wishlistItem = wishlistMapper.toEntity(request);
        wishlistItem.setProduct(product);
        wishlist.addWishlistItem(wishlistItem);

        wishlist = wishlistRepository.save(wishlist);
        return wishlistMapper.toResponse(wishlist, htmlEscapeUtil);
    }

    @Transactional
    public WishlistResponse removeFromWishlist(Long userId, Long itemId) {
        Wishlist wishlist = getUserWishlistOrThrow(userId);

        WishlistItem itemToRemove = wishlistItemRepository.findById(itemId)
                .orElseThrow(() -> new WishlistNotFoundException("Wishlist item not found"));

        if (!itemToRemove.getWishlist().getId().equals(wishlist.getId())) {
            throw new IllegalArgumentException("Item does not belong to user's wishlist");
        }

        wishlist.removeWishlistItem(itemToRemove);
        wishlist = wishlistRepository.save(wishlist);

        return wishlistMapper.toResponse(wishlist, htmlEscapeUtil);
    }

    @Transactional
    public WishlistResponse clearWishlist(Long userId) {
        Wishlist wishlist = getUserWishlistOrThrow(userId);

        wishlist.getWishlistItems().clear();
        wishlist = wishlistRepository.save(wishlist);

        return wishlistMapper.toResponse(wishlist, htmlEscapeUtil);
    }

    @Transactional
    public CartResponse moveToCart(Long userId, Long itemId) {
        Wishlist wishlist = getUserWishlistOrThrow(userId);

        WishlistItem itemToMove = wishlistItemRepository.findById(itemId)
                .orElseThrow(() -> new WishlistNotFoundException("Wishlist item not found"));

        if (!itemToMove.getWishlist().getId().equals(wishlist.getId())) {
            throw new IllegalArgumentException("Item does not belong to user's wishlist");
        }

        CartItemRequest cartItemRequest = new CartItemRequest();
        cartItemRequest.setProductId(itemToMove.getProduct().getId());
        cartItemRequest.setQuantity(1);

        wishlist.removeWishlistItem(itemToMove);
        wishlistRepository.save(wishlist);

        return cartService.addToCart(userId, cartItemRequest);
    }

    private Wishlist getOrCreateWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wishlist newWishlist = new Wishlist();
                    newWishlist.setUserId(userId);
                    return wishlistRepository.save(newWishlist);
                });
    }

    private Wishlist getUserWishlistOrThrow(Long userId) {
        return wishlistRepository.findByUserId(userId)
                .orElseThrow(() -> new WishlistNotFoundException("Wishlist not found"));
    }
}