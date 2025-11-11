package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.cart.CartItemRequest;
import com.marketnest.ecommerce.dto.cart.CartResponse;
import com.marketnest.ecommerce.dto.cart.UpdateCartItemRequest;
import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.cart.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Cart Management", description = "APIs for managing the shopping cart")
public class CartController {
    private final CartService cartService;
    private final UserRepository userRepository;

    @Operation(summary = "Get user cart",
            description = "Retrieves the cart of the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CartResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CartResponse> getUserCart(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(cartService.getUserCart(userId));
    }

    @Operation(summary = "Add item to cart",
            description = "Adds an item to the authenticated user's cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item added to cart successfully",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
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

    @Operation(summary = "Update cart item quantity",
            description = "Updates the quantity of an item in the authenticated user's cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item quantity updated successfully",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
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

    @Operation(summary = "Remove item from cart",
            description = "Removes an item from the authenticated user's cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item removed from cart successfully",
                    content = @Content(schema = @Schema(implementation = CartResponse.class)))
    })
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeCartItem(
            Authentication authentication,
            @PathVariable Long itemId) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(cartService.removeCartItem(userId, itemId));
    }

    @Operation(summary = "Clear cart",
            description = "Clears all items from the authenticated user's cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart cleared successfully",
                    content = @Content(schema = @Schema(implementation = CartResponse.class)))
    })
    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(cartService.clearCart(userId));
    }

    @Operation(summary = "Get cart items count",
            description = "Retrieves the count of items in the authenticated user's cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Cart items count retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getCartItemsCount(Authentication authentication) {
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