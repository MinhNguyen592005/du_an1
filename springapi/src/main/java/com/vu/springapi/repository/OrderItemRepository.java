package com.vu.springapi.repository;

import com.vu.springapi.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT COALESCE(SUM(oi.quantity), 0) " +
            "FROM OrderItem oi " +
            "WHERE oi.order.placedAt BETWEEN :from AND :to " +
            "AND oi.order.status IN ('completed', 'delivered')")
    Long countProductsSold(@Param("from") LocalDateTime from,
                           @Param("to") LocalDateTime to);

    // Top 5 sản phẩm bán chạy (chỉ từ đơn đã hoàn thành)
    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity), oi.product.imageUrl " +
           "FROM OrderItem oi " +
           "WHERE oi.order.placedAt BETWEEN :from AND :to " +
           "AND oi.order.status IN ('completed', 'delivered') " +
           "GROUP BY oi.product.id, oi.product.name, oi.product.imageUrl " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> getTopSellingProducts(@Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to);
}
