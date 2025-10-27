package com.marketnest.ecommerce.service.order;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
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
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();

            // Define colors and fonts
            BaseColor primaryColor = new BaseColor(41, 128, 185);
            BaseColor lightGray = new BaseColor(236, 240, 241);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, primaryColor);
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.DARK_GRAY);

            // Header section
            Paragraph companyName = new Paragraph("MarketNest", headerFont);
            companyName.setAlignment(Element.ALIGN_LEFT);
            document.add(companyName);

            Paragraph invoiceTitle = new Paragraph("INVOICE #" + order.getId(), subHeaderFont);
            invoiceTitle.setAlignment(Element.ALIGN_RIGHT);
            document.add(invoiceTitle);

            Paragraph invoiceDate = new Paragraph("Date: " + order.getOrderDate().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")), normalFont);
            invoiceDate.setAlignment(Element.ALIGN_RIGHT);
            document.add(invoiceDate);

            // Add separator line
            LineSeparator ls = new LineSeparator();
            ls.setLineColor(primaryColor);
            ls.setLineWidth(1.5f);
            document.add(new Chunk(ls));

            document.add(new Paragraph("\n"));

            // Customer and shipping info in a table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);

            // Customer information
            PdfPCell customerCell = new PdfPCell();
            customerCell.setBorder(Rectangle.NO_BORDER);
            customerCell.addElement(new Paragraph("BILLED TO:", boldFont));
            customerCell.addElement(new Paragraph(order.getUser().getFirstName() + " " +
                                                  order.getUser().getLastName(), normalFont));
            customerCell.addElement(new Paragraph(order.getUser().getEmail(), normalFont));

            Address billingAddress = order.getBillingAddress() != null ?
                    order.getBillingAddress() : order.getShippingAddress();
            customerCell.addElement(new Paragraph(formatAddress(billingAddress), normalFont));
            infoTable.addCell(customerCell);

            // Shipping information
            PdfPCell shippingCell = new PdfPCell();
            shippingCell.setBorder(Rectangle.NO_BORDER);
            shippingCell.addElement(new Paragraph("SHIPPED TO:", boldFont));
            shippingCell.addElement(
                    new Paragraph(formatAddress(order.getShippingAddress()), normalFont));
            infoTable.addCell(shippingCell);

            document.add(infoTable);
            document.add(new Paragraph("\n"));

            // Order items table with styling
            PdfPTable itemsTable = new PdfPTable(5);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{3f, 2f, 1f, 2f, 2f});

            // Table headers with styling
            String[] headers = {"Product", "Variant", "Quantity", "Unit Price", "Total"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
                cell.setBackgroundColor(primaryColor);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                cell.setBorderColor(BaseColor.WHITE);
                itemsTable.addCell(cell);
            }

            // Table data with alternating row colors
            boolean alternate = false;
            for (OrderItem item : order.getOrderItems()) {
                BaseColor rowColor = alternate ? lightGray : BaseColor.WHITE;
                alternate = !alternate;

                // Product name
                PdfPCell cell = new PdfPCell(new Phrase(item.getProduct().getName(), normalFont));
                cell.setBackgroundColor(rowColor);
                cell.setPadding(5);
                itemsTable.addCell(cell);

                // Variant
                cell = new PdfPCell(new Phrase(item.getVariant().getOption1Name(), normalFont));
                cell.setBackgroundColor(rowColor);
                cell.setPadding(5);
                itemsTable.addCell(cell);

                // Quantity
                cell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                cell.setBackgroundColor(rowColor);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                itemsTable.addCell(cell);

                // Unit price
                cell = new PdfPCell(new Phrase(formatCurrency(item.getUnitPrice()), normalFont));
                cell.setBackgroundColor(rowColor);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setPadding(5);
                itemsTable.addCell(cell);

                // Total price
                cell = new PdfPCell(new Phrase(formatCurrency(item.getTotalPrice()), normalFont));
                cell.setBackgroundColor(rowColor);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setPadding(5);
                itemsTable.addCell(cell);
            }

            document.add(itemsTable);
            document.add(new Paragraph("\n"));

            // Order summary table - right aligned
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(40);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryTable.setSpacingBefore(10);

            // Add summary rows
            addSummaryRow(summaryTable, "Subtotal:", order.getSubtotal(), normalFont, boldFont);
            addSummaryRow(summaryTable, "Shipping:", order.getShippingCost(), normalFont, boldFont);
            addSummaryRow(summaryTable, "Tax:", order.getTax(), normalFont, boldFont);
            addSummaryRow(summaryTable, "Discount:", order.getDiscount(), normalFont, boldFont);

            // Add total with different styling
            PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL:", boldFont));
            totalLabelCell.setBorder(Rectangle.TOP);
            totalLabelCell.setPaddingTop(5);
            summaryTable.addCell(totalLabelCell);

            PdfPCell totalValueCell = new PdfPCell(new Phrase(formatCurrency(order.getTotal()),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, primaryColor)));
            totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalValueCell.setBorder(Rectangle.TOP);
            totalValueCell.setPaddingTop(5);
            summaryTable.addCell(totalValueCell);

            document.add(summaryTable);

            // Add thank you message
            Paragraph thankYou = new Paragraph("\nThanks for your Trust!",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, primaryColor));
            thankYou.setAlignment(Element.ALIGN_CENTER);
            document.add(thankYou);

            // Add footer
            Rectangle pageSize = document.getPageSize();
            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_CENTER,
                    new Phrase("MarketNest · support@marketnest.com", smallFont),
                    pageSize.getWidth() / 2,
                    pageSize.getBottom() + 20,
                    0);

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    private void addSummaryRow(PdfPTable table, String label, BigDecimal value,
                               Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(formatCurrency(value), valueFont));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(valueCell);
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