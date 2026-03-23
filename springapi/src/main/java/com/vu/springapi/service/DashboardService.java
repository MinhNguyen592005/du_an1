package com.vu.springapi.service;

import com.vu.springapi.dto.response.DailyRevenueResponse;
import com.vu.springapi.dto.response.DashboardSummaryResponse;
import com.vu.springapi.dto.response.TopProductResponse;
import com.vu.springapi.repository.OrderItemRepository;
import com.vu.springapi.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(LocalDateTime from, LocalDateTime to) {
        log.info("Getting dashboard summary from {} to {}", from, to);

        BigDecimal revenue = orderRepository.sumRevenue(from, to);
        Long orders = orderRepository.countOrders(from, to);
        Long customers = orderRepository.countCustomers(from, to);
        Long productsSold = orderItemRepository.countProductsSold(from, to);

        return DashboardSummaryResponse.builder()
                .revenue(revenue != null ? revenue : BigDecimal.ZERO)
                .totalOrders(orders != null ? orders : 0L)
                .totalCustomers(customers != null ? customers : 0L)
                .totalProductsSold(productsSold != null ? productsSold : 0L)
                .build();
    }

    @Transactional(readOnly = true)
    public List<DailyRevenueResponse> getDailyRevenue(LocalDateTime from, LocalDateTime to) {
        log.info("Getting daily revenue from {} to {}", from, to);

        List<Object[]> results = orderRepository.getDailyRevenue(from, to);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM");

        return results.stream()
                .map(row -> DailyRevenueResponse.builder()
                        .date(((java.sql.Date) row[0]).toLocalDate().format(formatter))
                        .revenue((BigDecimal) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TopProductResponse> getTopSellingProducts(LocalDateTime from, LocalDateTime to, int limit) {
        log.info("Getting top {} selling products from {} to {}", limit, from, to);

        List<Object[]> results = orderItemRepository.getTopSellingProducts(from, to);

        return results.stream()
                .limit(limit)
                .map(row -> TopProductResponse.builder()
                        .productId((Long) row[0])
                        .productName((String) row[1])
                        .quantitySold((Long) row[2])
                        .productImage((String) row[3])
                        .build())
                .collect(Collectors.toList());
    }
}

