package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.dto.payment.PaymentMethodDto;
import com.marketnest.ecommerce.dto.payment.PaymentProcessRequestDto;
import com.marketnest.ecommerce.dto.payment.PaymentResponseDto;
import com.marketnest.ecommerce.dto.payment.RefundRequestDto;
import com.marketnest.ecommerce.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "APIs for managing payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Process a payment", description = "Processes a payment for an order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment processed successfully",
                    content = @Content(
                            schema = @Schema(implementation = PaymentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    @PostMapping("/process")
    public ResponseEntity<?> processPayment(
            @Valid @RequestBody PaymentProcessRequestDto requestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        log.info("Received payment processing request for order ID: {}", requestDto.getOrderId());
        PaymentResponseDto response = paymentService.processPayment(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get payment by ID", description = "Retrieves payment details by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = PaymentResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable Long paymentId) {
        log.info("Received request to fetch payment with ID: {}", paymentId);
        PaymentResponseDto response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refund a payment",
            description = "Processes a refund for a specific payment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refund processed successfully",
                    content = @Content(
                            schema = @Schema(implementation = PaymentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponseDto> refundPayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody RefundRequestDto requestDto) {

        log.info("Received refund request for payment ID: {}", paymentId);
        PaymentResponseDto response = paymentService.refundPayment(paymentId, requestDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get available payment methods",
            description = "Retrieves a list of available payment methods.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Payment methods retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaymentMethodDto.class)))
    })
    @GetMapping("/methods")
    public ResponseEntity<List<PaymentMethodDto>> getAvailablePaymentMethods() {
        log.info("Received request to fetch available payment methods");
        List<PaymentMethodDto> methods = paymentService.getAvailablePaymentMethods();
        return ResponseEntity.ok(methods);
    }
}