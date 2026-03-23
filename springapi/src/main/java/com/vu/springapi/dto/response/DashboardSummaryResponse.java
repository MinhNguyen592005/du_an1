package com.vu.springapi.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryResponse {

    private BigDecimal revenue;
    private Long totalOrders;
    private Long totalCustomers;
    private Long totalProductsSold;
}
