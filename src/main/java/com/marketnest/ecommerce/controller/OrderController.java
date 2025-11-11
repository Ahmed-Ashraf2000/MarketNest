package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.dto.order.OrderRequestDto;
import com.marketnest.ecommerce.dto.order.OrderResponseDto;
import com.marketnest.ecommerce.dto.order.OrderSummaryDto;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @Operation(summary = "Get user orders",
            description = "Retrieves a paginated list of orders for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OrderSummaryDto.class)))
    })
    @GetMapping
    public ResponseEntity<Page<OrderSummaryDto>> getUserOrders(
            @PageableDefault(size = 10, sort = "orderDate") Pageable pageable,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        Page<OrderSummaryDto> orders = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get order details",
            description = "Retrieves details of a specific order by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderDetails(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        OrderResponseDto order = orderService.getOrderDetails(orderId, userId);
        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Create an order", description = "Creates a new order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody OrderRequestDto orderRequestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        OrderResponseDto createdOrder = orderService.createOrder(orderRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @Operation(summary = "Cancel an order", description = "Cancels an order by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        OrderResponseDto cancelledOrder = orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(cancelledOrder);
    }

    @Operation(summary = "Track an order", description = "Tracks the status of an order by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order tracked successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}/track")
    public ResponseEntity<OrderResponseDto> trackOrder(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        OrderResponseDto order = orderService.trackOrder(orderId, userId);
        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Download order invoice",
            description = "Downloads the invoice for a specific order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invoice downloaded successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE)),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        byte[] invoicePdf = orderService.generateInvoicePdf(orderId, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("invoice-" + orderId + ".pdf").build());

        return new ResponseEntity<>(invoicePdf, headers, HttpStatus.OK);
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("The user not found"));

        return user.getUserId();
    }
}