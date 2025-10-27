package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.dto.payment.PaymentMethodDto;
import com.marketnest.ecommerce.dto.payment.PaymentProcessRequestDto;
import com.marketnest.ecommerce.dto.payment.PaymentResponseDto;
import com.marketnest.ecommerce.dto.payment.RefundRequestDto;
import com.marketnest.ecommerce.service.payment.PaymentService;
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
public class PaymentController {

    private final PaymentService paymentService;

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

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable Long paymentId) {
        log.info("Received request to fetch payment with ID: {}", paymentId);
        PaymentResponseDto response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponseDto> refundPayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody RefundRequestDto requestDto) {

        log.info("Received refund request for payment ID: {}", paymentId);
        PaymentResponseDto response = paymentService.refundPayment(paymentId, requestDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/methods")
    public ResponseEntity<List<PaymentMethodDto>> getAvailablePaymentMethods() {
        log.info("Received request to fetch available payment methods");
        List<PaymentMethodDto> methods = paymentService.getAvailablePaymentMethods();
        return ResponseEntity.ok(methods);
    }
}