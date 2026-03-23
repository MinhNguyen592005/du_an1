package com.vu.springapi.repository;

import com.vu.springapi.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Tìm theo user (OrderService đang gọi)
    Page<Order> findByUser_Id(Long userId, Pageable pageable);

    // Tổng doanh thu (chỉ đơn đã hoàn thành)
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.placedAt BETWEEN :from AND :to AND o.status IN ('completed', 'delivered')")
    BigDecimal sumRevenue(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Tổng số đơn (chỉ đơn đã hoàn thành)
    @Query("SELECT COUNT(o.id) FROM Order o WHERE o.placedAt BETWEEN :from AND :to AND o.status IN ('completed', 'delivered')")
    Long countOrders(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Tổng số khách (đếm user khác nhau, chỉ đơn đã hoàn thành)
    @Query("SELECT COUNT(DISTINCT o.user.id) FROM Order o WHERE o.placedAt BETWEEN :from AND :to AND o.status IN ('completed', 'delivered')")
    Long countCustomers(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Doanh thu theo ngày (cho biểu đồ, chỉ đơn đã hoàn thành)
    @Query("SELECT DATE(o.placedAt) as date, COALESCE(SUM(o.total), 0) as revenue " +
           "FROM Order o " +
           "WHERE o.placedAt BETWEEN :from AND :to AND o.status IN ('completed', 'delivered') " +
           "GROUP BY DATE(o.placedAt) " +
           "ORDER BY DATE(o.placedAt)")
    List<Object[]> getDailyRevenue(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
