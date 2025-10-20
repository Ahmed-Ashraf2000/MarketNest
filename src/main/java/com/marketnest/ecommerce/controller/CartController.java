package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.cart.CartItemRequest;
import com.marketnest.ecommerce.dto.cart.CartResponse;
import com.marketnest.ecommerce.dto.cart.UpdateCartItemRequest;
import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.cart.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<CartResponse> getUserCart(
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(cartService.getUserCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<?> addToCart(
            Authentication authentication,
            @Valid @RequestBody CartItemRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        Long userId = extractUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addToCart(userId, request));
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<?> updateCartItemQuantity(
            Authentication authentication,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        Long userId = extractUserId(authentication);
        if (request.getQuantity() == null || request.getQuantity() < 1) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                cartService.updateCartItemQuantity(userId, itemId, request.getQuantity()));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeCartItem(
            Authentication authentication,
            @PathVariable Long itemId) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(cartService.removeCartItem(userId, itemId));
    }

    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(cartService.clearCart(userId));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getCartItemsCount(
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        int count = cartService.getCartItemsCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    private Long extractUserId(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("The user not found"));

        return user.getUserId();
    }
}