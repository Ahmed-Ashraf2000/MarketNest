package com.marketnest.ecommerce.service.payment;

import com.marketnest.ecommerce.dto.payment.PaymentMethodDto;
import com.marketnest.ecommerce.dto.payment.PaymentProcessRequestDto;
import com.marketnest.ecommerce.dto.payment.PaymentResponseDto;
import com.marketnest.ecommerce.dto.payment.RefundRequestDto;

import java.util.List;

public interface PaymentService {
    PaymentResponseDto processPayment(PaymentProcessRequestDto requestDto);

    PaymentResponseDto getPaymentById(Long paymentId);

    PaymentResponseDto refundPayment(Long paymentId, RefundRequestDto requestDto);

    List<PaymentMethodDto> getAvailablePaymentMethods();
}