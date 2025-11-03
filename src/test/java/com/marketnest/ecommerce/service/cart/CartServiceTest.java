package com.marketnest.ecommerce.service.cart;

import com.marketnest.ecommerce.dto.cart.CartItemRequest;
import com.marketnest.ecommerce.dto.cart.CartResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private HtmlEscapeUtil htmlEscapeUtil;

    @InjectMocks
    private CartService cartService;

    private Cart testCart;
    private Product testProduct;
    private CartItemRequest cartItemRequest;
    private CartResponse cartResponse;

    @BeforeEach
    void setUp() {
        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUserId(1L);
        testCart.setStatus(Cart.CartStatus.ACTIVE);
        testCart.setTotalPrice(BigDecimal.ZERO);
        testCart.setCartItems(new ArrayList<>());

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));

        cartItemRequest = new CartItemRequest();
        cartItemRequest.setProductId(1L);
        cartItemRequest.setQuantity(2);

        cartResponse = new CartResponse();
        cartResponse.setId(1L);
        cartResponse.setTotalPrice(new BigDecimal("199.98"));
    }

    @Test
    void getUserCart_shouldReturnExistingCart() {
        when(cartRepository.findByUserIdAndStatus(1L, Cart.CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));
        when(cartMapper.toResponse(testCart, htmlEscapeUtil)).thenReturn(cartResponse);

        CartResponse result = cartService.getUserCart(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(cartRepository).findByUserIdAndStatus(1L, Cart.CartStatus.ACTIVE);
    }

    @Test
    void getUserCart_shouldCreateNewCart_whenNotExists() {
        when(cartRepository.findByUserIdAndStatus(1L, Cart.CartStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartMapper.toResponse(testCart, htmlEscapeUtil)).thenReturn(cartResponse);

        CartResponse result = cartService.getUserCart(1L);

        assertThat(result).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addToCart_shouldAddNewItem_whenProductNotInCart() {
        CartItem cartItem = new CartItem();
        cartItem.setProduct(testProduct);
        cartItem.setQuantity(2);
        cartItem.setPrice(testProduct.getPrice());

        when(cartRepository.findByUserIdAndStatus(1L, Cart.CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartMapper.toEntity(cartItemRequest)).thenReturn(cartItem);
        when(cartRepository.save(testCart)).thenReturn(testCart);
        when(cartMapper.toResponse(testCart, htmlEscapeUtil)).thenReturn(cartResponse);

        CartResponse result = cartService.addToCart(1L, cartItemRequest);

        assertThat(result).isNotNull();
        verify(cartRepository).save(testCart);
    }

    @Test
    void addToCart_shouldIncrementQuantity_whenProductAlreadyInCart() {
        CartItem existingItem = new CartItem();
        existingItem.setId(1L);
        existingItem.setProduct(testProduct);
        existingItem.setQuantity(1);
        existingItem.setPrice(testProduct.getPrice());
        testCart.getCartItems().add(existingItem);

        when(cartRepository.findByUserIdAndStatus(1L, Cart.CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.save(existingItem)).thenReturn(existingItem);
        when(cartMapper.toResponse(testCart, htmlEscapeUtil)).thenReturn(cartResponse);

        CartResponse result = cartService.addToCart(1L, cartItemRequest);

        assertThat(result).isNotNull();
        assertThat(existingItem.getQuantity()).isEqualTo(3);
        verify(cartItemRepository).save(existingItem);
    }

    @Test
    void addToCart_shouldThrowException_whenProductNotFound() {
        when(cartRepository.findByUserIdAndStatus(1L, Cart.CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addToCart(1L, cartItemRequest))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void updateCartItemQuantity_shouldUpdateQuantity() {
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setProduct(testProduct);
        cartItem.setQuantity(2);
        cartItem.setPrice(testProduct.getPrice());
        testCart.getCartItems().add(cartItem);

        when(cartRepository.findByUserIdAndStatus(1L, Cart.CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
        when(cartRepository.save(testCart)).thenReturn(testCart);
        when(cartMapper.toResponse(testCart, htmlEscapeUtil)).thenReturn(cartResponse);

        CartResponse result = cartService.updateCartItemQuantity(1L, 1L, 5);

        assertThat(result).isNotNull();
        assertThat(cartItem.getQuantity()).isEqualTo(5);
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    void updateCartItemQuantity_shouldThrowException_whenItemNotFound() {
        when(cartRepository.findByUserIdAndStatus(1L, Cart.CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));

        assertThatThrownBy(() -> cartService.updateCartItemQuantity(1L, 999L, 5))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cart item not found");
    }

    @Test
    void updateCartItemQuantity_shouldThrowException_whenCartNotFound() {
        when(cartRepository.findByUserIdAndStatus(anyLong(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateCartItemQuantity(999L, 1L, 5))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("Active cart not found");
    }

    @Test
    void removeCartItem_shouldRemoveItem() {
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setProduct(testProduct);
        cartItem.setQuantity(2);
        testCart.getCartItems().add(cartItem);

        when(cartRepository.findByUserIdAndStatus(1L, Cart.CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));
        when(cartRepository.save(testCart)).thenReturn(testCart);
        when(cartMapper.toResponse(testCart, htmlEscapeUtil)).thenReturn(cartResponse);

        CartResponse result = cartService.removeCartItem(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(testCart.getCartItems()).isEmpty();
        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void removeCartItem_shouldThrowException_whenItemNotFound() {
        when(cartRepository.findByUserIdAndStatus(1L, Cart.CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));

        assertThatThrownBy(() -> cartService.removeCartItem(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cart item not found");
    }

    @Test
    void clearCart_shouldRemoveAllItems() {
        CartItem item1 = new CartItem();
        CartItem item2 = new CartItem();
        testCart.getCartItems().add(item1);
        testCart.getCartItems().add(item2);

        when(cartRepository.findByUserIdAndStatus(1L, Cart.CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));
        when(cartRepository.save(testCart)).thenReturn(testCart);
        when(cartMapper.toResponse(testCart, htmlEscapeUtil)).thenReturn(cartResponse);

        CartResponse result = cartService.clearCart(1L);

        assertThat(result).isNotNull();
        assertThat(testCart.getCartItems()).isEmpty();
        assertThat(testCart.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(cartRepository).save(testCart);
    }

    @Test
    void getCartItemsCount_shouldReturnCorrectCount() {
        testCart.getCartItems().add(new CartItem());
        testCart.getCartItems().add(new CartItem());
        testCart.getCartItems().add(new CartItem());

        when(cartRepository.findByUserIdAndStatus(1L, Cart.CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));

        int count = cartService.getCartItemsCount(1L);

        assertThat(count).isEqualTo(3);
    }

    @Test
    void getCartItemsCount_shouldReturnZero_whenCartEmpty() {
        when(cartRepository.findByUserIdAndStatus(1L, Cart.CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));

        int count = cartService.getCartItemsCount(1L);

        assertThat(count).isZero();
    }
}