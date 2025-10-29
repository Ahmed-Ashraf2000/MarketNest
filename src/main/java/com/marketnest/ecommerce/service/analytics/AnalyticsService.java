package com.marketnest.ecommerce.service.analytics;

import com.marketnest.ecommerce.dto.analytics.*;
import com.marketnest.ecommerce.model.Order;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.OrderItemRepository;
import com.marketnest.ecommerce.repository.OrderRepository;
import com.marketnest.ecommerce.repository.ProductRepository;
import com.marketnest.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    public DashboardOverviewDto getDashboardOverview() {
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        Long totalOrders = orderRepository.count();
        Long totalCustomers =
                userRepository.countByRole(User.Role.CUSTOMER);
        Long totalProducts = productRepository.count();
        BigDecimal averageOrderValue = orderRepository.calculateAverageOrderValue();
        Long pendingOrders = orderRepository.countByStatus(Order.OrderStatus.PENDING);
        Long lowStockProducts = productRepository.countLowStockProducts();

        RevenueComparisonDto revenueComparison = getRevenueComparison();
        OrdersComparisonDto ordersComparison = getOrdersComparison();

        return new DashboardOverviewDto(
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                totalOrders,
                totalCustomers,
                totalProducts,
                averageOrderValue != null ? averageOrderValue : BigDecimal.ZERO,
                pendingOrders,
                lowStockProducts,
                revenueComparison,
                ordersComparison
        );
    }

    public List<SalesStatisticsDto> getSalesStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        return orderRepository.findSalesStatisticsNative(start, end).stream()
                .map(result -> new SalesStatisticsDto(
                        ((java.sql.Date) result[0]).toLocalDate(),
                        ((Number) result[1]).longValue(),
                        (BigDecimal) result[2],
                        (BigDecimal) result[3]
                ))
                .collect(Collectors.toList());
    }


    public List<TopSellingProductDto> getTopSellingProducts(int limit) {
        return orderItemRepository.findTopSellingProducts(limit);
    }

    public List<LowStockProductDto> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public List<RevenueReportDto> getRevenueReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        return orderRepository.findRevenueReportNative(start, end).stream()
                .map(result -> new RevenueReportDto(
                        ((java.sql.Date) result[0]).toLocalDate(),
                        (BigDecimal) result[1],
                        (BigDecimal) result[2],
                        ((Number) result[3]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public CustomerStatisticsDto getCustomerStatistics() {
        Long totalCustomers =
                userRepository.countByRole(com.marketnest.ecommerce.model.User.Role.CUSTOMER);

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        Long newCustomersThisMonth = userRepository.countNewCustomersSince(startOfMonth);

        Long customersWithOrders = orderRepository.countDistinctCustomers();
        Long activeCustomers = userRepository.countActiveCustomers();

        Double retentionRate = totalCustomers > 0
                ? (customersWithOrders.doubleValue() / totalCustomers.doubleValue()) * 100
                : 0.0;

        return new CustomerStatisticsDto(
                totalCustomers,
                newCustomersThisMonth,
                activeCustomers,
                retentionRate,
                customersWithOrders
        );
    }

    public List<OrderStatusStatisticsDto> getOrderStatusStatistics() {
        long totalOrders = orderRepository.count();

        return orderRepository.findOrderCountByStatus().stream()
                .map(result -> {
                    Order.OrderStatus status = (Order.OrderStatus) result[0];
                    Long count = (Long) result[1];
                    Double percentage = totalOrders > 0
                            ? (count.doubleValue() / (double) totalOrders) * 100
                            : 0.0;
                    return new OrderStatusStatisticsDto(status, count, percentage);
                })
                .collect(Collectors.toList());
    }

    private RevenueComparisonDto getRevenueComparison() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfCurrentMonth =
                now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);

        BigDecimal currentRevenue =
                orderRepository.calculateRevenueForPeriod(startOfCurrentMonth, now);
        BigDecimal previousRevenue = orderRepository.calculateRevenueForPeriod(startOfPreviousMonth,
                startOfCurrentMonth);

        BigDecimal percentageChange = BigDecimal.ZERO;
        if (previousRevenue != null && previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
            percentageChange = currentRevenue.subtract(previousRevenue)
                    .divide(previousRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return new RevenueComparisonDto(
                currentRevenue != null ? currentRevenue : BigDecimal.ZERO,
                previousRevenue != null ? previousRevenue : BigDecimal.ZERO,
                percentageChange
        );
    }

    private OrdersComparisonDto getOrdersComparison() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfCurrentMonth =
                now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);

        Long currentOrders = orderRepository.countOrdersForPeriod(startOfCurrentMonth, now);
        Long previousOrders =
                orderRepository.countOrdersForPeriod(startOfPreviousMonth, startOfCurrentMonth);

        double percentageChange = 0.0;
        if (previousOrders > 0) {
            percentageChange =
                    ((currentOrders - previousOrders) / previousOrders.doubleValue()) * 100;
        }

        return new OrdersComparisonDto(currentOrders, previousOrders, percentageChange);
    }
}