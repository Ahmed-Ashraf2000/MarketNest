package com.marketnest.ecommerce.service.payment;

import com.marketnest.ecommerce.dto.payment.PaymentMethodDto;
import com.marketnest.ecommerce.dto.payment.PaymentProcessRequestDto;
import com.marketnest.ecommerce.dto.payment.PaymentResponseDto;
import com.marketnest.ecommerce.dto.payment.RefundRequestDto;
import com.marketnest.ecommerce.exception.OrderNotFoundException;
import com.marketnest.ecommerce.exception.PaymentNotFoundException;
import com.marketnest.ecommerce.mapper.payment.PaymentMapper;
import com.marketnest.ecommerce.model.Order;
import com.marketnest.ecommerce.model.Payment;
import com.marketnest.ecommerce.repository.OrderRepository;
import com.marketnest.ecommerce.repository.PaymentRepository;
import com.marketnest.ecommerce.util.HtmlEscapeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private HtmlEscapeUtil htmlEscapeUtil;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Order testOrder;
    private Payment testPayment;
    private PaymentProcessRequestDto paymentProcessRequestDto;
    private PaymentResponseDto paymentResponseDto;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setTotal(new BigDecimal("114.99"));

        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setOrder(testOrder);
        testPayment.setAmount(new BigDecimal("114.99"));
        testPayment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        testPayment.setStatus(Payment.PaymentStatus.PENDING);
        testPayment.setTransactionId("TXN123456");

        paymentProcessRequestDto = new PaymentProcessRequestDto();
        paymentProcessRequestDto.setOrderId(1L);
        paymentProcessRequestDto.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD.toString());
        paymentProcessRequestDto.setStripeToken("tok_visa");

        paymentResponseDto = new PaymentResponseDto();
        paymentResponseDto.setId(1L);
        paymentResponseDto.setAmount(new BigDecimal("114.99"));
        paymentResponseDto.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD.toString());
        paymentResponseDto.setStatus(Payment.PaymentStatus.COMPLETED.toString());
        paymentResponseDto.setTransactionId("TXN123456");
    }

    @Test
    void processPayment_shouldProcessSuccessfully_withCreditCard() {
        testPayment.setStatus(Payment.PaymentStatus.COMPLETED);
        testPayment.setPaymentDate(LocalDateTime.now());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentMapper.toResponse(testPayment, htmlEscapeUtil)).thenReturn(paymentResponseDto);

        PaymentResponseDto result = paymentService.processPayment(paymentProcessRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED.toString());
        verify(orderRepository).findById(1L);
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void processPayment_shouldThrowException_whenOrderNotFound() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.processPayment(paymentProcessRequestDto))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void processPayment_shouldHandleCashOnDelivery() {
        paymentProcessRequestDto.setPaymentMethod(
                Payment.PaymentMethod.CASH_ON_DELIVERY.toString());
        paymentProcessRequestDto.setStripeToken(null);
        testPayment.setPaymentMethod(Payment.PaymentMethod.CASH_ON_DELIVERY);
        testPayment.setStatus(Payment.PaymentStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentMapper.toResponse(testPayment, htmlEscapeUtil)).thenReturn(paymentResponseDto);

        PaymentResponseDto result = paymentService.processPayment(paymentProcessRequestDto);

        assertThat(result).isNotNull();
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void getPaymentById_shouldReturnPayment_whenExists() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentMapper.toResponse(testPayment, htmlEscapeUtil)).thenReturn(paymentResponseDto);

        PaymentResponseDto result = paymentService.getPaymentById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(paymentRepository).findById(1L);
    }

    @Test
    void getPaymentById_shouldThrowException_whenNotFound() {
        when(paymentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById(999L))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("Payment not found");
    }

    @Test
    void refundPayment_shouldProcessRefund_whenPaymentCompleted() {
        testPayment.setStatus(Payment.PaymentStatus.COMPLETED);
        RefundRequestDto refundRequestDto = new RefundRequestDto();
        refundRequestDto.setReason("Customer request");
        refundRequestDto.setAmount(new BigDecimal("114.99"));

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentMapper.toResponse(testPayment, htmlEscapeUtil)).thenReturn(paymentResponseDto);

        PaymentResponseDto result = paymentService.refundPayment(1L, refundRequestDto);

        assertThat(result).isNotNull();
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void refundPayment_shouldThrowException_whenPaymentNotFound() {
        RefundRequestDto refundRequestDto = new RefundRequestDto();
        refundRequestDto.setReason("Customer request");

        when(paymentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.refundPayment(999L, refundRequestDto))
                .isInstanceOf(PaymentNotFoundException.class);

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void getAvailablePaymentMethods_shouldReturnAllMethods() {
        List<PaymentMethodDto> result = paymentService.getAvailablePaymentMethods();

        assertThat(result).isNotEmpty();
        assertThat(result).hasSizeGreaterThanOrEqualTo(5);
        assertThat(result).extracting(PaymentMethodDto::getName)
                .contains("CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "BANK_TRANSFER",
                        "CASH_ON_DELIVERY");
    }

    @Test
    void processPayment_shouldSetPaymentDate_whenSuccessful() {
        testPayment.setStatus(Payment.PaymentStatus.COMPLETED);
        testPayment.setPaymentDate(LocalDateTime.now());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentMapper.toResponse(testPayment, htmlEscapeUtil)).thenReturn(paymentResponseDto);

        paymentService.processPayment(paymentProcessRequestDto);

        verify(paymentRepository, times(2)).save(argThat(payment ->
                payment.getPaymentDate() != null
        ));
    }
}