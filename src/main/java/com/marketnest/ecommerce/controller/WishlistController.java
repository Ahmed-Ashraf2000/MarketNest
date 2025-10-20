package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.cart.CartResponse;
import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.dto.wishlist.AddWishlistItemRequest;
import com.marketnest.ecommerce.dto.wishlist.WishlistResponse;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.wishlist.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<WishlistResponse> getUserWishlist(
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(wishlistService.getUserWishlist(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<?> addToWishlist(
            Authentication authentication,
            @Valid @RequestBody AddWishlistItemRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(wishlistService.addToWishlist(userId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<WishlistResponse> removeFromWishlist(
            Authentication authentication,
            @PathVariable Long itemId) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(wishlistService.removeFromWishlist(userId, itemId));
    }

    @DeleteMapping
    public ResponseEntity<WishlistResponse> clearWishlist(
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(wishlistService.clearWishlist(userId));
    }

    @PostMapping("/items/{itemId}/move-to-cart")
    public ResponseEntity<CartResponse> moveToCart(
            Authentication authentication,
            @PathVariable Long itemId) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(wishlistService.moveToCart(userId, itemId));
    }

    private Long extractUserId(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("The user not found"));

        return user.getUserId();
    }
}