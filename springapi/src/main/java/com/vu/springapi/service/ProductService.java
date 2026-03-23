package com.vu.springapi.service;

import com.vu.springapi.dto.request.ProductCreateRequest;
import com.vu.springapi.dto.request.ProductUpdateRequest;
import com.vu.springapi.exception.AppException;
import com.vu.springapi.exception.ErrorCode;
import com.vu.springapi.model.Category;
import com.vu.springapi.model.Product;
import com.vu.springapi.repository.CategoryRepository;
import com.vu.springapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime; // <-- Cần thiết cho Product Entity
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    // ************************
    // 1. READ (Tìm kiếm, Lọc, Phân trang) - CÔNG KHAI
    // ************************
    @Transactional(readOnly = true)
    public Page<Product> getProductsFiltered(String keyword, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<Product> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Chỉ lấy sản phẩm đang hoạt động (isActive = true HOẶC isActive là NULL)
            // Đây là Predicate cuối cùng để khắc phục lỗi không hiển thị sản phẩm
            Predicate activePredicate = criteriaBuilder.or(
                criteriaBuilder.equal(root.get("isActive"), true),
                criteriaBuilder.isNull(root.get("isActive")) 
            );
            predicates.add(activePredicate);
            
            // 2. Lọc theo Keyword (Giữ nguyên logic của bạn)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword.toLowerCase() + "%";
                
                Predicate keywordPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), searchPattern) 
                );
                
                predicates.add(keywordPredicate);
            }

            // 3. Lọc theo Category ID (Giữ nguyên logic của bạn)
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable);
    }
    
    // 2. CREATE (Tạo mới) - ADMIN
    public Product createProduct(ProductCreateRequest request) {
        // Kiểm tra SKU đã tồn tại
        if (request.getSku() != null && productRepository.existsBySku(request.getSku())) {
            throw new AppException(ErrorCode.PRODUCT_SKU_EXISTED);
        }

        // Tìm Category
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Chuyển đổi DTO sang Entity và lưu (Sử dụng Builder pattern)
        Product product = Product.builder()
            .name(request.getName())
            .sku(request.getSku())
            .description(request.getDescription())
            .price(request.getPrice())
            .stock(request.getStock())
            .imageUrl(request.getImageUrl())
            .category(category)
            .isActive(Boolean.TRUE)
            .createdAt(LocalDateTime.now()) // <-- Set created_at
            .build();

        return productRepository.save(product);
    }

    // 3. READ (Lấy chi tiết)
    @Transactional(readOnly = true)
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    // 4. UPDATE (Cập nhật) - ADMIN
    public Product updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = getProductById(productId);

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getStock() != null) product.setStock(request.getStock());
        if (request.getIsActive() != null) product.setIsActive(request.getIsActive()); // Cập nhật trạng thái
        
        // Cập nhật Category
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            product.setCategory(category);
        }

        return productRepository.save(product);
    }

    // 5. DELETE (Xóa) - ADMIN
    public void deleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        productRepository.deleteById(productId);
    }
}