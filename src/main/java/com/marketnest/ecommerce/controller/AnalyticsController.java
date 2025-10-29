package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.analytics.*;
import com.marketnest.ecommerce.service.analytics.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardOverviewDto> getDashboardOverview() {
        return ResponseEntity.ok(analyticsService.getDashboardOverview());
    }

    @GetMapping("/sales")
    public ResponseEntity<List<SalesStatisticsDto>> getSalesStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(analyticsService.getSalesStatistics(startDate, endDate));
    }

    @GetMapping("/products/top-selling")
    public ResponseEntity<List<TopSellingProductDto>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(analyticsService.getTopSellingProducts(limit));
    }

    @GetMapping("/products/low-stock")
    public ResponseEntity<List<LowStockProductDto>> getLowStockProducts() {
        return ResponseEntity.ok(analyticsService.getLowStockProducts());
    }

    @GetMapping("/revenue")
    public ResponseEntity<List<RevenueReportDto>> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(analyticsService.getRevenueReport(startDate, endDate));
    }

    @GetMapping("/customers")
    public ResponseEntity<CustomerStatisticsDto> getCustomerStatistics() {
        return ResponseEntity.ok(analyticsService.getCustomerStatistics());
    }

    @GetMapping("/orders/status")
    public ResponseEntity<List<OrderStatusStatisticsDto>> getOrderStatusStatistics() {
        return ResponseEntity.ok(analyticsService.getOrderStatusStatistics());
    }
}