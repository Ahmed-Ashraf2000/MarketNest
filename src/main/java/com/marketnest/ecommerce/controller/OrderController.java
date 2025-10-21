package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.dto.order.OrderRequestDto;
import com.marketnest.ecommerce.dto.order.OrderResponseDto;
import com.marketnest.ecommerce.dto.order.OrderSummaryDto;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.order.OrderService;
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
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<OrderSummaryDto>> getUserOrders(
            @PageableDefault(size = 10, sort = "orderDate") Pageable pageable,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        Page<OrderSummaryDto> orders = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderDetails(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        OrderResponseDto order = orderService.getOrderDetails(orderId, userId);
        return ResponseEntity.ok(order);
    }

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

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        OrderResponseDto cancelledOrder = orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(cancelledOrder);
    }

    @GetMapping("/{orderId}/track")
    public ResponseEntity<OrderResponseDto> trackOrder(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        OrderResponseDto order = orderService.trackOrder(orderId, userId);
        return ResponseEntity.ok(order);
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("The user not found"));

        return user.getUserId();
    }

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
}