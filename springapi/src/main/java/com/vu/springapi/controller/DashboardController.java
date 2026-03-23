package com.vu.springapi.controller;

import com.vu.springapi.dto.response.ApiResponse;
import com.vu.springapi.dto.response.DailyRevenueResponse;
import com.vu.springapi.dto.response.DashboardSummaryResponse;
import com.vu.springapi.dto.response.TopProductResponse;
import com.vu.springapi.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * API tổng quan dashboard
     * GET /dashboard/summary?from=2025-11-01&to=2025-11-30
     */
    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> getSummary(
            @RequestParam String from,
            @RequestParam String to
    ) {
        log.info("API: Get dashboard summary from {} to {}", from, to);

        LocalDateTime start = LocalDate.parse(from).atStartOfDay();
        LocalDateTime end = LocalDate.parse(to).atTime(23, 59, 59);

        return ApiResponse.<DashboardSummaryResponse>builder()
                .result(dashboardService.getSummary(start, end))
                .build();
    }

    /**
     * API biểu đồ doanh thu theo ngày (7 ngày gần nhất)
     * GET /dashboard/daily-revenue?from=2025-11-19&to=2025-11-25
     */
    @GetMapping("/daily-revenue")
    public ApiResponse<List<DailyRevenueResponse>> getDailyRevenue(
            @RequestParam String from,
            @RequestParam String to
    ) {
        log.info("API: Get daily revenue from {} to {}", from, to);

        LocalDateTime start = LocalDate.parse(from).atStartOfDay();
        LocalDateTime end = LocalDate.parse(to).atTime(23, 59, 59);

        return ApiResponse.<List<DailyRevenueResponse>>builder()
                .result(dashboardService.getDailyRevenue(start, end))
                .build();
    }

    /**
     * API Top 5 sản phẩm bán chạy
     * GET /dashboard/top-products?from=2025-11-01&to=2025-11-30&limit=5
     */
    @GetMapping("/top-products")
    public ApiResponse<List<TopProductResponse>> getTopProducts(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "5") int limit
    ) {
        log.info("API: Get top {} products from {} to {}", limit, from, to);

        LocalDateTime start = LocalDate.parse(from).atStartOfDay();
        LocalDateTime end = LocalDate.parse(to).atTime(23, 59, 59);

        return ApiResponse.<List<TopProductResponse>>builder()
                .result(dashboardService.getTopSellingProducts(start, end, limit))
                .build();
    }
}
