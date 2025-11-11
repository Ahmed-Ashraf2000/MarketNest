package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.cart.CartResponse;
import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.dto.wishlist.AddWishlistItemRequest;
import com.marketnest.ecommerce.dto.wishlist.WishlistResponse;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.wishlist.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Wishlist Management", description = "APIs for managing user wishlists")
public class WishlistController {
    private final WishlistService wishlistService;
    private final UserRepository userRepository;

    @Operation(summary = "Get user wishlist",
            description = "Retrieves the wishlist of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wishlist retrieved successfully",
                    content = @Content(schema = @Schema(implementation = WishlistResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping
    public ResponseEntity<WishlistResponse> getUserWishlist(
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(wishlistService.getUserWishlist(userId));
    }

    @Operation(summary = "Add item to wishlist",
            description = "Adds an item to the wishlist of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item added to wishlist successfully",
                    content = @Content(schema = @Schema(implementation = WishlistResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
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

    @Operation(summary = "Remove item from wishlist",
            description = "Removes an item from the wishlist of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Item removed from wishlist successfully",
                    content = @Content(schema = @Schema(implementation = WishlistResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<WishlistResponse> removeFromWishlist(
            Authentication authentication,
            @PathVariable Long itemId) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(wishlistService.removeFromWishlist(userId, itemId));
    }

    @Operation(summary = "Clear wishlist",
            description = "Clears all items from the wishlist of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wishlist cleared successfully",
                    content = @Content(schema = @Schema(implementation = WishlistResponse.class)))
    })
    @DeleteMapping
    public ResponseEntity<WishlistResponse> clearWishlist(
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(wishlistService.clearWishlist(userId));
    }

    @Operation(summary = "Move item to cart",
            description = "Moves an item from the wishlist to the cart of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item moved to cart successfully",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
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