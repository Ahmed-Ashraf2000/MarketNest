package com.marketnest.ecommerce.service.order;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.marketnest.ecommerce.dto.order.OrderRequestDto;
import com.marketnest.ecommerce.dto.order.OrderResponseDto;
import com.marketnest.ecommerce.dto.order.OrderSummaryDto;
import com.marketnest.ecommerce.exception.OrderNotFoundException;
import com.marketnest.ecommerce.mapper.order.OrderMapper;
import com.marketnest.ecommerce.model.*;
import com.marketnest.ecommerce.repository.*;
import com.marketnest.ecommerce.util.HtmlEscapeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final OrderMapper orderMapper;
    private final HtmlEscapeUtil htmlEscapeUtil;

    public Page<OrderSummaryDto> getUserOrders(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUser_UserId(userId, pageable);
        return orders.map(order -> orderMapper.toSummary(order, htmlEscapeUtil));
    }

    public OrderResponseDto getOrderDetails(Long orderId, Long userId) {
        Order order = orderRepository.findByUser_UserIdAndId(userId, orderId)
                .orElseThrow(
                        () -> new OrderNotFoundException("Order not found with id: " + orderId));

        return orderMapper.toResponse(order, htmlEscapeUtil);
    }

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        Address shippingAddress =
                addressRepository.findAddressByUser_UserIdAndId(
                                user.getUserId(),
                                orderRequestDto.getShippingAddressId())
                        .orElseThrow(
                                () -> new IllegalArgumentException("Invalid shipping address"));

        Address billingAddress = null;
        if (orderRequestDto.getBillingAddressId() != null) {
            billingAddress =
                    addressRepository.findAddressByUser_UserIdAndId(
                                    user.getUserId(),
                                    orderRequestDto.getBillingAddressId())
                            .orElseThrow(
                                    () -> new IllegalArgumentException("Invalid billing address"));
        }

        Order order = orderMapper.toEntity(orderRequestDto, htmlEscapeUtil);
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress(shippingAddress);
        order.setBillingAddress(billingAddress);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (var itemDto : orderRequestDto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Product not found: " + itemDto.getProductId()));

            ProductVariant variant = variantRepository.findById(itemDto.getVariantId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Variant not found: " + itemDto.getVariantId()));

            if (!variant.getProduct().getId().equals(product.getId())) {
                throw new IllegalArgumentException("Variant does not belong to specified product");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setVariant(variant);
            orderItem.setQuantity(itemDto.getQuantity());

            BigDecimal unitPrice =
                    variant.getPrice() != null ? variant.getPrice() : product.getPrice();
            orderItem.setUnitPrice(unitPrice);

            BigDecimal totalItemPrice =
                    unitPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            orderItem.setTotalPrice(totalItemPrice);

            subtotal = subtotal.add(totalItemPrice);

            order.addOrderItem(orderItem);
        }

        order.setSubtotal(subtotal);
        order.setShippingCost(
                BigDecimal.valueOf(10.00));
        order.setTax(subtotal.multiply(BigDecimal.valueOf(0.14)));
        order.setDiscount(BigDecimal.ZERO);

        BigDecimal total = subtotal
                .add(order.getShippingCost())
                .add(order.getTax())
                .subtract(order.getDiscount());
        order.setTotal(total);

        OrderStatusHistory statusHistory = new OrderStatusHistory();
        statusHistory.setStatus(Order.OrderStatus.PENDING);
        statusHistory.setNotes("Order created");
        statusHistory.setCreatedBy(email);
        order.addStatusHistory(statusHistory);

        Order savedOrder = orderRepository.save(order);

        return orderMapper.toResponse(savedOrder, htmlEscapeUtil);
    }

    @Transactional
    public OrderResponseDto cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByUser_UserIdAndId(userId, orderId)
                .orElseThrow(
                        () -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != Order.OrderStatus.PENDING &&
            order.getStatus() != Order.OrderStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);

        OrderStatusHistory statusHistory = new OrderStatusHistory();
        statusHistory.setStatus(Order.OrderStatus.CANCELLED);
        statusHistory.setNotes("Order cancelled by customer");
        statusHistory.setCreatedBy(
                SecurityContextHolder.getContext().getAuthentication().getName());
        order.addStatusHistory(statusHistory);

        Order savedOrder = orderRepository.save(order);

        return orderMapper.toResponse(savedOrder, htmlEscapeUtil);
    }

    public byte[] generateInvoicePdf(Long orderId, Long userId) {
        Order order = orderRepository.findByUser_UserIdAndId(userId, orderId)
                .orElseThrow(
                        () -> new OrderNotFoundException("Order not found with id: " + orderId));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add  header
            document.add(new Paragraph("MarketNest"));
            document.add(new Paragraph("Invoice #" + order.getId()));
            document.add(new Paragraph("Date: " + order.getOrderDate().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
            document.add(new Paragraph("\n"));

            // Add customer information
            document.add(new Paragraph("Customer: " + order.getUser().getFirstName() + " " +
                                       order.getUser().getLastName()));
            document.add(new Paragraph("Email: " + order.getUser().getEmail()));
            document.add(new Paragraph("\n"));

            // Add shipping address
            document.add(new Paragraph("Shipping Address:"));
            document.add(new Paragraph(formatAddress(order.getShippingAddress())));

            // Add billing address if different
            if (order.getBillingAddress() != null &&
                !order.getBillingAddress().getId().equals(order.getShippingAddress().getId())) {
                document.add(new Paragraph("Billing Address:"));
                document.add(new Paragraph(formatAddress(order.getBillingAddress())));
            }
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            // Set table headers
            table.addCell("Product");
            table.addCell("Variant");
            table.addCell("Quantity");
            table.addCell("Unit Price");
            table.addCell("Total");

            // Add order items
            for (OrderItem item : order.getOrderItems()) {
                table.addCell(item.getProduct().getName());
                table.addCell(item.getVariant().getOption1Name());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell(formatCurrency(item.getUnitPrice()));
                table.addCell(formatCurrency(item.getTotalPrice()));
            }

            document.add(table);
            document.add(new Paragraph("\n"));

            // Add order summary
            document.add(new Paragraph("Subtotal: " + formatCurrency(order.getSubtotal())));
            document.add(new Paragraph("Shipping: " + formatCurrency(order.getShippingCost())));
            document.add(new Paragraph("Tax: " + formatCurrency(order.getTax())));
            document.add(new Paragraph("Discount: " + formatCurrency(order.getDiscount())));
            document.add(new Paragraph("Total: " + formatCurrency(order.getTotal())));

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Thank you for your purchase!"));

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    private String formatAddress(Address address) {
        return address.getAddressLine1() + "\n" +
               address.getCity() + ", " + address.getStateProvince() + " " + address.getPostalCode() + "\n" +
               address.getCountryCode();
    }

    private String formatCurrency(BigDecimal amount) {
        return "$" + amount.setScale(2, RoundingMode.HALF_UP);
    }


    public OrderResponseDto trackOrder(Long orderId, Long userId) {
        return getOrderDetails(orderId, userId);
    }
}