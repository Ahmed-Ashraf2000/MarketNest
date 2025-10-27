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
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Refund;
import com.stripe.param.ChargeCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final HtmlEscapeUtil htmlEscapeUtil;

    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentProcessRequestDto requestDto) {
        log.info("Processing payment for order ID: {}", requestDto.getOrderId());

        Order order = orderRepository.findById(requestDto.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found with ID: " + requestDto.getOrderId()));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotal());
        payment.setPaymentMethod(Payment.PaymentMethod.valueOf(requestDto.getPaymentMethod()));
        payment.setStatus(Payment.PaymentStatus.PENDING);

        try {
            if ("CREDIT_CARD".equals(requestDto.getPaymentMethod()) ||
                "DEBIT_CARD".equals(requestDto.getPaymentMethod())) {

                Charge charge = processStripePayment(
                        order.getTotal(),
                        requestDto.getStripeToken(),
                        requestDto.getStripePaymentMethodId(),
                        requestDto.getEmail(),
                        order.getId()
                );

                payment.setTransactionId(charge.getId());
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setPaymentDate(LocalDateTime.now());

                order.setStatus(Order.OrderStatus.PROCESSING);
                orderRepository.save(order);

                log.info("Payment processed successfully for order ID: {}", order.getId());
            } else if ("CASH_ON_DELIVERY".equals(requestDto.getPaymentMethod())) {
                payment.setTransactionId("COD-" + order.getId() + "-" + System.currentTimeMillis());
                payment.setStatus(Payment.PaymentStatus.PENDING);
                payment.setPaymentDate(LocalDateTime.now());

                order.setStatus(Order.OrderStatus.PROCESSING);
                orderRepository.save(order);

                log.info("Cash on delivery payment registered for order ID: {}", order.getId());
            } else {
                throw new IllegalArgumentException("Unsupported payment method: " +
                                                   requestDto.getPaymentMethod());
            }

            Payment savedPayment = paymentRepository.save(payment);
            return paymentMapper.toResponse(savedPayment, htmlEscapeUtil);

        } catch (StripeException e) {
            log.error("Stripe payment failed for order ID: {}", order.getId(), e);
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    private Charge processStripePayment(BigDecimal amount, String token,
                                        String paymentMethodId, String email, Long orderId)
            throws StripeException {

        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

        ChargeCreateParams.Builder paramsBuilder = ChargeCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("usd")
                .setDescription("Payment for Order #" + orderId);

        if (token != null && !token.isBlank()) {
            paramsBuilder.setSource(token);
        } else if (paymentMethodId != null && !paymentMethodId.isBlank()) {
            paramsBuilder.setSource(paymentMethodId);
        } else {
            throw new IllegalArgumentException(
                    "Either Stripe token or payment method ID is required");
        }

        if (email != null && !email.isBlank()) {
            paramsBuilder.setReceiptEmail(email);
        }

        return Charge.create(paramsBuilder.build());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Long paymentId) {
        log.info("Fetching payment with ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found with ID: " + paymentId));

        return paymentMapper.toResponse(payment, htmlEscapeUtil);
    }

    @Override
    @Transactional
    public PaymentResponseDto refundPayment(Long paymentId, RefundRequestDto requestDto) {
        log.info("Processing refund for payment ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found with ID: " + paymentId));

        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded");
        }

        try {
            BigDecimal refundAmount = requestDto.getAmount() != null ?
                    requestDto.getAmount() : payment.getAmount();

            if (refundAmount.compareTo(payment.getAmount()) > 0) {
                throw new IllegalArgumentException("Refund amount cannot exceed payment amount");
            }

            if (payment.getTransactionId().startsWith("COD-")) {
                log.info("Processing COD refund for payment ID: {}", paymentId);
                payment.setStatus(Payment.PaymentStatus.REFUNDED);
            } else {
                Refund refund = processStripeRefund(
                        payment.getTransactionId(),
                        refundAmount,
                        requestDto.getReason()
                );

                payment.setStatus(Payment.PaymentStatus.REFUNDED);
                log.info("Stripe refund processed successfully: {}", refund.getId());
            }

            Order order = payment.getOrder();
            order.setStatus(Order.OrderStatus.CANCELLED);
            orderRepository.save(order);

            Payment savedPayment = paymentRepository.save(payment);
            return paymentMapper.toResponse(savedPayment, htmlEscapeUtil);

        } catch (StripeException e) {
            log.error("Stripe refund failed for payment ID: {}", paymentId, e);
            throw new RuntimeException("Refund processing failed: " + e.getMessage(), e);
        }
    }

    private Refund processStripeRefund(String chargeId, BigDecimal amount, String reason)
            throws StripeException {

        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

        RefundCreateParams params = RefundCreateParams.builder()
                .setCharge(chargeId)
                .setAmount(amountInCents)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .putMetadata("reason", reason)
                .build();

        return Refund.create(params);
    }

    @Override
    public List<PaymentMethodDto> getAvailablePaymentMethods() {
        log.info("Fetching available payment methods");

        return Arrays.asList(
                new PaymentMethodDto("CREDIT_CARD", "Credit Card",
                        "Pay securely with your credit card via Stripe", true),
                new PaymentMethodDto("DEBIT_CARD", "Debit Card",
                        "Pay securely with your debit card via Stripe", true),
                new PaymentMethodDto("PAYPAL", "PayPal",
                        "Pay with your PayPal account", false),
                new PaymentMethodDto("BANK_TRANSFER", "Bank Transfer",
                        "Direct bank transfer", false),
                new PaymentMethodDto("CASH_ON_DELIVERY", "Cash on Delivery",
                        "Pay with cash upon delivery", true)
        );
    }
}