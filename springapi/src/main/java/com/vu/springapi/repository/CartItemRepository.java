package com.vu.springapi.repository;

import com.vu.springapi.model.Cart;
import com.vu.springapi.model.CartItem;
import com.vu.springapi.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // Tìm cart item theo cart và product
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    
    // Tìm cart item theo cartId và productId
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
    Optional<CartItem> findByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);
    
    // Lấy tất cả cart items của một cart
    List<CartItem> findByCart(Cart cart);
    
    // Đếm số lượng item trong cart
    Integer countByCart(Cart cart);
    
    // Xóa tất cả item trong cart
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart = :cart")
    void deleteByCart(@Param("cart") Cart cart);
    
    // Xóa item cụ thể theo cartId và productId
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
    void deleteByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);
    
    // Tính tổng số lượng sản phẩm trong cart
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer getTotalQuantityByCartId(@Param("cartId") Long cartId);

    List<CartItem> findByCartId(Long id);
}