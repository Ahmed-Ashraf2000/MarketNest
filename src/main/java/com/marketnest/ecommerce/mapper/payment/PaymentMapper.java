package com.marketnest.ecommerce.mapper.payment;

import com.marketnest.ecommerce.dto.payment.PaymentRequestDto;
import com.marketnest.ecommerce.dto.payment.PaymentResponseDto;
import com.marketnest.ecommerce.model.Payment;
import com.marketnest.ecommerce.util.HtmlEscapeUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "paymentMethod",
            expression = "java(Payment.PaymentMethod.valueOf(paymentRequestDto.getPaymentMethod()))")
    @Mapping(target = "transactionId",
            expression = "java(htmlEscapeUtil.escapeHtml(paymentRequestDto.getTransactionId()))")
    Payment toEntity(PaymentRequestDto paymentRequestDto, HtmlEscapeUtil htmlEscapeUtil);

    @Mapping(target = "paymentMethod", expression = "java(payment.getPaymentMethod().name())")
    @Mapping(target = "status", expression = "java(payment.getStatus().name())")
    @Mapping(target = "transactionId",
            expression = "java(htmlEscapeUtil.escapeHtml(payment.getTransactionId()))")
    PaymentResponseDto toResponse(Payment payment, HtmlEscapeUtil htmlEscapeUtil);

    default List<PaymentResponseDto> toResponseList(List<Payment> payments,
                                                    HtmlEscapeUtil htmlEscapeUtil) {
        if (payments == null) {
            return null;
        }

        return payments.stream()
                .map(payment -> toResponse(payment, htmlEscapeUtil))
                .toList();
    }
}