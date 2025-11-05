package com.marketnest.ecommerce.service.analytics;

import com.marketnest.ecommerce.dto.analytics.*;
import com.marketnest.ecommerce.model.Order;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.OrderItemRepository;
import com.marketnest.ecommerce.repository.OrderRepository;
import com.marketnest.ecommerce.repository.ProductRepository;
import com.marketnest.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void getDashboardOverview_ShouldReturnCompleteOverview() {
        BigDecimal totalRevenue = new BigDecimal("50000.00");
        BigDecimal averageOrderValue = new BigDecimal("500.00");

        when(orderRepository.calculateTotalRevenue()).thenReturn(totalRevenue);
        when(orderRepository.count()).thenReturn(100L);
        when(userRepository.countByRole(User.Role.CUSTOMER)).thenReturn(50L);
        when(productRepository.count()).thenReturn(200L);
        when(orderRepository.calculateAverageOrderValue()).thenReturn(averageOrderValue);
        when(orderRepository.countByStatus(Order.OrderStatus.PENDING)).thenReturn(10L);
        when(productRepository.countLowStockProducts()).thenReturn(5L);

        when(orderRepository.calculateRevenueForPeriod(any(), any()))
                .thenReturn(new BigDecimal("10000.00"))
                .thenReturn(new BigDecimal("8000.00"));
        when(orderRepository.countOrdersForPeriod(any(), any()))
                .thenReturn(20L)
                .thenReturn(15L);

        DashboardOverviewDto result = analyticsService.getDashboardOverview();

        assertNotNull(result);
        assertEquals(totalRevenue, result.getTotalRevenue());
        assertEquals(100L, result.getTotalOrders());
        assertEquals(50L, result.getTotalCustomers());
        assertEquals(200L, result.getTotalProducts());
        assertEquals(averageOrderValue, result.getAverageOrderValue());
        assertEquals(10L, result.getPendingOrders());
        assertEquals(5L, result.getLowStockProducts());
        assertNotNull(result.getRevenueComparison());
        assertNotNull(result.getOrdersComparison());

        verify(orderRepository, times(1)).calculateTotalRevenue();
        verify(userRepository, times(1)).countByRole(User.Role.CUSTOMER);
    }

    @Test
    void getDashboardOverview_WithNullValues_ShouldReturnDefaultValues() {
        when(orderRepository.calculateTotalRevenue()).thenReturn(null);
        when(orderRepository.count()).thenReturn(0L);
        when(userRepository.countByRole(User.Role.CUSTOMER)).thenReturn(0L);
        when(productRepository.count()).thenReturn(0L);
        when(orderRepository.calculateAverageOrderValue()).thenReturn(null);
        when(orderRepository.countByStatus(Order.OrderStatus.PENDING)).thenReturn(0L);
        when(productRepository.countLowStockProducts()).thenReturn(0L);
        when(orderRepository.calculateRevenueForPeriod(any(), any())).thenReturn(null);
        when(orderRepository.countOrdersForPeriod(any(), any())).thenReturn(0L);

        DashboardOverviewDto result = analyticsService.getDashboardOverview();

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalRevenue());
        assertEquals(BigDecimal.ZERO, result.getAverageOrderValue());
    }

    @Test
    void getSalesStatistics_ShouldReturnStatisticsForDateRange() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        List<Object[]> mockResults = Arrays.asList(
                new Object[]{Date.valueOf("2024-01-01"), 10L, new BigDecimal("5000.00"),
                        new BigDecimal("500.00")},
                new Object[]{Date.valueOf("2024-01-02"), 15L, new BigDecimal("7500.00"),
                        new BigDecimal("500.00")}
        );

        when(orderRepository.findSalesStatisticsNative(any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(mockResults);

        List<SalesStatisticsDto> result = analyticsService.getSalesStatistics(startDate, endDate);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(LocalDate.of(2024, 1, 1), result.getFirst().getDate());
        assertEquals(10L, result.getFirst().getOrderCount());
        assertEquals(new BigDecimal("5000.00"), result.getFirst().getTotalSales());

        verify(orderRepository, times(1)).findSalesStatisticsNative(any(), any());
    }

    @Test
    void getTopSellingProducts_ShouldReturnLimitedList() {
        int limit = 5;
        List<TopSellingProductDto> mockProducts = Arrays.asList(
                new TopSellingProductDto(1L, "Product 1", "product1", 100L,
                        new BigDecimal("10000.00"), "imgUrl"),
                new TopSellingProductDto(2L, "Product 2", "product2", 80L,
                        new BigDecimal("8000.00"), "imgUrl")
        );

        when(orderItemRepository.findTopSellingProducts(limit)).thenReturn(mockProducts);

        List<TopSellingProductDto> result = analyticsService.getTopSellingProducts(limit);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Product 1", result.getFirst().getProductName());
        verify(orderItemRepository, times(1)).findTopSellingProducts(limit);
    }

    @Test
    void getLowStockProducts_ShouldReturnProductsList() {
        List<LowStockProductDto> mockProducts = Arrays.asList(
                new LowStockProductDto(1L, "Low Stock Product", "low-stock-product", 5, 10,
                        "imgUrl"),
                new LowStockProductDto(2L, "Another Low Stock", "another-low-stock", 2, 20,
                        "imgUrl")
        );

        when(productRepository.findLowStockProducts()).thenReturn(mockProducts);

        List<LowStockProductDto> result = analyticsService.getLowStockProducts();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Low Stock Product", result.getFirst().getProductName());
        verify(productRepository, times(1)).findLowStockProducts();
    }

    @Test
    void getRevenueReport_ShouldReturnReportForDateRange() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        List<RevenueReportDto> result = analyticsService.getRevenueReport(startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(new BigDecimal("5000.00"), result.getFirst().getRevenue());
        assertEquals(10L, result.getFirst().getOrderCount());
        verify(orderRepository, times(1)).findRevenueReportNative(any(), any());
    }

    @Test
    void getCustomerStatistics_ShouldReturnCompleteStatistics() {
        when(userRepository.countByRole(User.Role.CUSTOMER)).thenReturn(100L);
        when(userRepository.countNewCustomersSince(any())).thenReturn(15L);
        when(orderRepository.countDistinctCustomers()).thenReturn(80L);
        when(userRepository.countActiveCustomers()).thenReturn(75L);

        CustomerStatisticsDto result = analyticsService.getCustomerStatistics();

        assertNotNull(result);
        assertEquals(100L, result.getTotalCustomers());
        assertEquals(15L, result.getNewCustomersThisMonth());
        assertEquals(75L, result.getActiveCustomers());
        assertEquals(80L, result.getCustomersWithOrders());

        verify(userRepository, times(1)).countByRole(User.Role.CUSTOMER);
        verify(orderRepository, times(1)).countDistinctCustomers();
    }

    @Test
    void getCustomerStatistics_WithZeroCustomers_ShouldHandleGracefully() {
        when(userRepository.countByRole(User.Role.CUSTOMER)).thenReturn(0L);
        when(userRepository.countNewCustomersSince(any())).thenReturn(0L);
        when(orderRepository.countDistinctCustomers()).thenReturn(0L);
        when(userRepository.countActiveCustomers()).thenReturn(0L);

        CustomerStatisticsDto result = analyticsService.getCustomerStatistics();

        assertNotNull(result);
    }

    @Test
    void getOrderStatusStatistics_ShouldReturnStatisticsWithPercentages() {
        when(orderRepository.count()).thenReturn(100L);

        List<Object[]> mockResults = Arrays.asList(
                new Object[]{Order.OrderStatus.PENDING, 20L},
                new Object[]{Order.OrderStatus.PROCESSING, 60L},
                new Object[]{Order.OrderStatus.CANCELLED, 20L}
        );

        when(orderRepository.findOrderCountByStatus()).thenReturn(mockResults);

        List<OrderStatusStatisticsDto> result = analyticsService.getOrderStatusStatistics();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(Order.OrderStatus.PENDING, result.getFirst().getStatus());
        assertEquals(20L, result.getFirst().getCount());
        assertEquals(20.0, result.getFirst().getPercentage(), 0.01);

        verify(orderRepository, times(1)).count();
        verify(orderRepository, times(1)).findOrderCountByStatus();
    }

    @Test
    void getOrderStatusStatistics_WithZeroOrders_ShouldReturnZeroPercentages() {
        when(orderRepository.count()).thenReturn(0L);
        when(orderRepository.findOrderCountByStatus()).thenReturn(List.of());

        List<OrderStatusStatisticsDto> result = analyticsService.getOrderStatusStatistics();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}