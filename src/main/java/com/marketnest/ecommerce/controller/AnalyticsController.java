package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.analytics.*;
import com.marketnest.ecommerce.service.analytics.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Analytics Management", description = "APIs for managing analytics data")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get dashboard overview",
            description = "Retrieves an overview of the dashboard analytics.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Dashboard overview retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = DashboardOverviewDto.class)))
    })
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardOverviewDto> getDashboardOverview() {
        return ResponseEntity.ok(analyticsService.getDashboardOverview());
    }

    @Operation(summary = "Get sales statistics",
            description = "Retrieves sales statistics for a given date range.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Sales statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SalesStatisticsDto.class)))
    })
    @GetMapping("/sales")
    public ResponseEntity<List<SalesStatisticsDto>> getSalesStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(analyticsService.getSalesStatistics(startDate, endDate));
    }

    @Operation(summary = "Get top-selling products",
            description = "Retrieves the top-selling products with an optional limit.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Top-selling products retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = TopSellingProductDto.class)))
    })
    @GetMapping("/products/top-selling")
    public ResponseEntity<List<TopSellingProductDto>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(analyticsService.getTopSellingProducts(limit));
    }

    @Operation(summary = "Get low-stock products",
            description = "Retrieves products that are low in stock.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Low-stock products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LowStockProductDto.class)))
    })
    @GetMapping("/products/low-stock")
    public ResponseEntity<List<LowStockProductDto>> getLowStockProducts() {
        return ResponseEntity.ok(analyticsService.getLowStockProducts());
    }

    @Operation(summary = "Get revenue report",
            description = "Retrieves revenue reports for a given date range.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Revenue report retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RevenueReportDto.class)))
    })
    @GetMapping("/revenue")
    public ResponseEntity<List<RevenueReportDto>> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(analyticsService.getRevenueReport(startDate, endDate));
    }

    @Operation(summary = "Get customer statistics",
            description = "Retrieves statistics about customers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Customer statistics retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = CustomerStatisticsDto.class)))
    })
    @GetMapping("/customers")
    public ResponseEntity<CustomerStatisticsDto> getCustomerStatistics() {
        return ResponseEntity.ok(analyticsService.getCustomerStatistics());
    }

    @Operation(summary = "Get order status statistics",
            description = "Retrieves statistics about order statuses.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Order status statistics retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = OrderStatusStatisticsDto.class)))
    })
    @GetMapping("/orders/status")
    public ResponseEntity<List<OrderStatusStatisticsDto>> getOrderStatusStatistics() {
        return ResponseEntity.ok(analyticsService.getOrderStatusStatistics());
    }
}