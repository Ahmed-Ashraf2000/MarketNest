package com.marketnest.ecommerce.service.wishlist;

import com.marketnest.ecommerce.dto.cart.CartResponse;
import com.marketnest.ecommerce.dto.wishlist.AddWishlistItemRequest;
import com.marketnest.ecommerce.dto.wishlist.WishlistResponse;
import com.marketnest.ecommerce.exception.ProductNotFoundException;
import com.marketnest.ecommerce.mapper.wishlist.WishlistMapper;
import com.marketnest.ecommerce.model.Product;
import com.marketnest.ecommerce.model.Wishlist;
import com.marketnest.ecommerce.model.WishlistItem;
import com.marketnest.ecommerce.repository.ProductRepository;
import com.marketnest.ecommerce.repository.WishlistItemRepository;
import com.marketnest.ecommerce.repository.WishlistRepository;
import com.marketnest.ecommerce.service.cart.CartService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartService cartService;

    @Mock
    private WishlistMapper wishlistMapper;

    @Mock
    private HtmlEscapeUtil htmlEscapeUtil;

    @InjectMocks
    private WishlistService wishlistService;

    private Wishlist testWishlist;
    private Product testProduct;
    private WishlistItem testItem;
    private AddWishlistItemRequest addRequest;
    private WishlistResponse wishlistResponse;

    @BeforeEach
    void setUp() {
        testWishlist = new Wishlist();
        testWishlist.setId(1L);
        testWishlist.setUserId(1L);
        testWishlist.setWishlistItems(new ArrayList<>());

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setIsActive(true);

        testItem = new WishlistItem();
        testItem.setId(1L);
        testItem.setWishlist(testWishlist);
        testItem.setProduct(testProduct);
        testItem.setPriority(WishlistItem.Priority.MEDIUM);

        addRequest = new AddWishlistItemRequest();
        addRequest.setProductId(1L);
        addRequest.setPriority(WishlistItem.Priority.HIGH);
        addRequest.setNotes("Test notes");

        wishlistResponse = new WishlistResponse();
        wishlistResponse.setUserId(1L);
        wishlistResponse.setTotalItems(1);
    }

    @Test
    void getUserWishlist_shouldReturnWishlist_whenExists() {
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(testWishlist));
        when(wishlistMapper.toResponse(testWishlist, htmlEscapeUtil)).thenReturn(wishlistResponse);

        WishlistResponse result = wishlistService.getUserWishlist(1L);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        verify(wishlistRepository).findByUserId(1L);
    }

    @Test
    void getUserWishlist_shouldCreateNewWishlist_whenNotExists() {
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(testWishlist);
        when(wishlistMapper.toResponse(testWishlist, htmlEscapeUtil)).thenReturn(wishlistResponse);

        WishlistResponse result = wishlistService.getUserWishlist(1L);

        assertThat(result).isNotNull();
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void addToWishlist_shouldAddItem_whenProductExists() {
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(testWishlist));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(testWishlist);
        when(wishlistMapper.toResponse(testWishlist, htmlEscapeUtil)).thenReturn(wishlistResponse);

        WishlistResponse result = wishlistService.addToWishlist(1L, addRequest);

        assertThat(result).isNotNull();
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void addToWishlist_shouldThrowException_whenProductNotFound() {
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(testWishlist));
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.addToWishlist(1L, addRequest))
                .isInstanceOf(ProductNotFoundException.class);

        verify(wishlistRepository, never()).save(any());
    }

    @Test
    void addToWishlist_shouldNotAddDuplicate_whenProductAlreadyInWishlist() {
        testWishlist.addWishlistItem(testItem);

        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(testWishlist));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(wishlistMapper.toResponse(testWishlist, htmlEscapeUtil)).thenReturn(wishlistResponse);

        WishlistResponse result = wishlistService.addToWishlist(1L, addRequest);

        assertThat(result).isNotNull();
        verify(wishlistRepository, never()).save(any());
    }

    @Test
    void removeFromWishlist_shouldRemoveItem_whenExists() {
        testWishlist.addWishlistItem(testItem);

        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(testWishlist));
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(testWishlist);
        when(wishlistMapper.toResponse(testWishlist, htmlEscapeUtil)).thenReturn(wishlistResponse);

        WishlistResponse result = wishlistService.removeFromWishlist(1L, 1L);

        assertThat(result).isNotNull();
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void removeFromWishlist_shouldThrowException_whenItemNotFound() {
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(testWishlist));

        assertThatThrownBy(() -> wishlistService.removeFromWishlist(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(wishlistRepository, never()).save(any());
    }

    @Test
    void clearWishlist_shouldRemoveAllItems() {
        testWishlist.addWishlistItem(testItem);

        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(testWishlist));
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(testWishlist);
        when(wishlistMapper.toResponse(testWishlist, htmlEscapeUtil)).thenReturn(wishlistResponse);

        WishlistResponse result = wishlistService.clearWishlist(1L);

        assertThat(result).isNotNull();
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void moveToCart_shouldAddToCartAndRemoveFromWishlist() {
        testWishlist.addWishlistItem(testItem);
        CartResponse cartResponse = new CartResponse();

        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(testWishlist));
        when(cartService.addToCart(anyLong(), any())).thenReturn(cartResponse);
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(testWishlist);

        CartResponse result = wishlistService.moveToCart(1L, 1L);

        assertThat(result).isNotNull();
        verify(cartService).addToCart(anyLong(), any());
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void moveToCart_shouldThrowException_whenItemNotFound() {
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(testWishlist));

        assertThatThrownBy(() -> wishlistService.moveToCart(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(cartService, never()).addToCart(anyLong(), any());
    }
}