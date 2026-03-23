package com.vu.springapi.repository;

import com.vu.springapi.model.Address;
import com.vu.springapi.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
// Kế thừa JpaSpecificationExecutor để hỗ trợ truy vấn tìm kiếm/lọc
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> { 
    
    // Cần thiết cho hàm createProduct (kiểm tra SKU đã tồn tại chưa)
    boolean existsBySku(String sku); 
}