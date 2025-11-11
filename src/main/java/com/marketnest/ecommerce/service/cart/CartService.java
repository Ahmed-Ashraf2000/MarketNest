package com.marketnest.ecommerce.service.cart;

import com.marketnest.ecommerce.dto.cart.CartItemRequest;
import com.marketnest.ecommerce.dto.cart.CartResponse;
import com.marketnest.ecommerce.exception.CartNotFoundException;
import com.marketnest.ecommerce.exception.CategoryNotFoundException;
import com.marketnest.ecommerce.exception.ProductNotFoundException;
import com.marketnest.ecommerce.mapper.cart.CartMapper;
import com.marketnest.ecommerce.model.Cart;
import com.marketnest.ecommerce.model.CartItem;
import com.marketnest.ecommerce.model.Product;
import com.marketnest.ecommerce.repository.CartItemRepository;
import com.marketnest.ecommerce.repository.CartRepository;
import com.marketnest.ecommerce.repository.ProductRepository;
import com.marketnest.ecommerce.util.HtmlEscapeUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;
    private final HtmlEscapeUtil htmlEscapeUtil;


    public CartResponse getUserCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return cartMapper.toResponse(cart, htmlEscapeUtil);
    }

    @Transactional
    public CartResponse addToCart(Long userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        for (CartItem item : cart.getCartItems()) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.incrementQuantity(request.getQuantity());
                cartItemRepository.save(item);
                cart.recalculateTotalPrice();
                return cartMapper.toResponse(cart, htmlEscapeUtil);
            }
        }

        CartItem cartItem = cartMapper.toEntity(request);
        cartItem.setProduct(product);
        cartItem.setPrice(product.getPrice());

        cart.addCartItem(cartItem);
        cartRepository.save(cart);

        return cartMapper.toResponse(cart, htmlEscapeUtil);
    }

    @Transactional
    public CartResponse updateCartItemQuantity(Long userId, Long itemId, Integer quantity) {
        Cart cart = getUserCartOrThrow(userId);

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException("Cart item not found"));

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        cart.recalculateTotalPrice();
        cartRepository.save(cart);

        return cartMapper.toResponse(cart, htmlEscapeUtil);
    }

    @Transactional
    public CartResponse removeCartItem(Long userId, Long itemId) {
        Cart cart = getUserCartOrThrow(userId);

        CartItem itemToRemove = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException("Cart item not found"));

        cart.removeCartItem(itemToRemove);
        cartItemRepository.delete(itemToRemove);
        cartRepository.save(cart);

        return cartMapper.toResponse(cart, htmlEscapeUtil);
    }

    @Transactional
    public CartResponse clearCart(Long userId) {
        Cart cart = getUserCartOrThrow(userId);

        cart.getCartItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        return cartMapper.toResponse(cart, htmlEscapeUtil);
    }

    public int getCartItemsCount(Long userId) {
        Cart cart = getUserCartOrThrow(userId);
        return cart.getCartItems().size();
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserIdAndStatus(userId, Cart.CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setStatus(Cart.CartStatus.ACTIVE);
                    return cartRepository.save(newCart);
                });
    }

    private Cart getUserCartOrThrow(Long userId) {
        return cartRepository.findByUserIdAndStatus(userId, Cart.CartStatus.ACTIVE)
                .orElseThrow(() -> new CategoryNotFoundException("Active cart not found"));
    }
}