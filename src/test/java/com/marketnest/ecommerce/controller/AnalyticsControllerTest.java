package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.analytics.*;
import com.marketnest.ecommerce.model.Order;
import com.marketnest.ecommerce.service.analytics.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @Test
    void getDashboardOverview_ShouldReturnOkWithData() throws Exception {
        DashboardOverviewDto dashboardDto = new DashboardOverviewDto(
                new BigDecimal("50000.00"),
                100L,
                50L,
                200L,
                new BigDecimal("500.00"),
                10L,
                5L,
                new RevenueComparisonDto(new BigDecimal("10000.00"), new BigDecimal("8000.00"),
                        new BigDecimal("25.00")),
                new OrdersComparisonDto(20L, 15L, 33.33)
        );

        when(analyticsService.getDashboardOverview()).thenReturn(dashboardDto);

        mockMvc.perform(get("/api/admin/analytics/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(50000.00))
                .andExpect(jsonPath("$.totalOrders").value(100))
                .andExpect(jsonPath("$.totalCustomers").value(50))
                .andExpect(jsonPath("$.pendingOrders").value(10));

        verify(analyticsService, times(1)).getDashboardOverview();
    }

    @Test
    void getSalesStatistics_ShouldReturnOkWithDateRange() throws Exception {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        List<SalesStatisticsDto> statistics = Arrays.asList(
                new SalesStatisticsDto(startDate, 10L, new BigDecimal("5000.00"),
                        new BigDecimal("500.00")),
                new SalesStatisticsDto(startDate.plusDays(1), 15L, new BigDecimal("7500.00"),
                        new BigDecimal("500.00"))
        );

        when(analyticsService.getSalesStatistics(startDate, endDate)).thenReturn(statistics);

        mockMvc.perform(get("/api/admin/analytics/sales")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderCount").value(10))
                .andExpect(jsonPath("$[0].totalRevenue").value(5000.00))
                .andExpect(jsonPath("$[1].orderCount").value(15));

        verify(analyticsService, times(1)).getSalesStatistics(startDate, endDate);
    }

    @Test
    void getTopSellingProducts_ShouldReturnOkWithLimit() throws Exception {
        List<TopSellingProductDto> products = Arrays.asList(
                new TopSellingProductDto(1L, "Product 1", "product1", 100L,
                        new BigDecimal("10000.00"), "imgUrl"),
                new TopSellingProductDto(2L, "Product 2", "product2", 80L,
                        new BigDecimal("8000.00"), "imgUrl")
        );

        when(analyticsService.getTopSellingProducts(10)).thenReturn(products);

        mockMvc.perform(get("/api/admin/analytics/products/top-selling")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Product 1"))
                .andExpect(jsonPath("$[0].totalSold").value(100))
                .andExpect(jsonPath("$[1].productName").value("Product 2"));

        verify(analyticsService, times(1)).getTopSellingProducts(10);
    }

    @Test
    void getTopSellingProducts_WithDefaultLimit_ShouldUseDefault() throws Exception {
        when(analyticsService.getTopSellingProducts(10)).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/analytics/products/top-selling")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(analyticsService, times(1)).getTopSellingProducts(10);
    }

    @Test
    void getLowStockProducts_ShouldReturnOk() throws Exception {
        List<LowStockProductDto> products = Arrays.asList(
                new LowStockProductDto(1L, "Low Stock Product", "Low-stock-product", 5, 10,
                        "imgUrl"),
                new LowStockProductDto(2L, "Another Low Stock", "another-low-stock", 2, 20,
                        "imgUrl")
        );

        when(analyticsService.getLowStockProducts()).thenReturn(products);

        mockMvc.perform(get("/api/admin/analytics/products/low-stock")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Low Stock Product"))
                .andExpect(jsonPath("$[0].currentStock").value(5))
                .andExpect(jsonPath("$[1].productName").value("Another Low Stock"));

        verify(analyticsService, times(1)).getLowStockProducts();
    }

    @Test
    void getRevenueReport_ShouldReturnOkWithDateRange() throws Exception {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        List<RevenueReportDto> report = List.of(
                new RevenueReportDto(startDate, new BigDecimal("5000.00"),
                        new BigDecimal("4500.00"), 10L)
        );

        when(analyticsService.getRevenueReport(startDate, endDate)).thenReturn(report);

        mockMvc.perform(get("/api/admin/analytics/revenue")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].grossRevenue").value(5000.00))
                .andExpect(jsonPath("$[0].netRevenue").value(4500.00))
                .andExpect(jsonPath("$[0].orderCount").value(10));

        verify(analyticsService, times(1)).getRevenueReport(startDate, endDate);
    }

    @Test
    void getCustomerStatistics_ShouldReturnOk() throws Exception {
        CustomerStatisticsDto statistics = new CustomerStatisticsDto(
                100L, 15L, 75L, 80.0, 80L
        );

        when(analyticsService.getCustomerStatistics()).thenReturn(statistics);

        mockMvc.perform(get("/api/admin/analytics/customers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").value(100))
                .andExpect(jsonPath("$.newCustomersThisMonth").value(15))
                .andExpect(jsonPath("$.activeCustomers").value(75))
                .andExpect(jsonPath("$.retentionRate").value(80.0));

        verify(analyticsService, times(1)).getCustomerStatistics();
    }

    @Test
    void getOrderStatusStatistics_ShouldReturnOk() throws Exception {
        List<OrderStatusStatisticsDto> statistics = Arrays.asList(
                new OrderStatusStatisticsDto(Order.OrderStatus.PENDING, 20L, 20.0),
                new OrderStatusStatisticsDto(Order.OrderStatus.PROCESSING, 60L, 60.0),
                new OrderStatusStatisticsDto(Order.OrderStatus.CANCELLED, 20L, 20.0)
        );

        when(analyticsService.getOrderStatusStatistics()).thenReturn(statistics);

        mockMvc.perform(get("/api/admin/analytics/orders/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].count").value(20))
                .andExpect(jsonPath("$[0].percentage").value(20.0))
                .andExpect(jsonPath("$[1].status").value("COMPLETED"));

        verify(analyticsService, times(1)).getOrderStatusStatistics();
    }
}