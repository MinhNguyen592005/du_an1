package com.vu.springapi.controller;

import com.vu.springapi.dto.request.ProductCreateRequest;
import com.vu.springapi.dto.request.ProductUpdateRequest;
import com.vu.springapi.dto.response.ApiResponse;
import com.vu.springapi.model.Product;
import com.vu.springapi.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    // 1. READ (Tìm kiếm, Lọc, Phân trang) - CÔNG KHAI
    @GetMapping
    public ApiResponse<Page<Product>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.<Page<Product>>builder()
            .result(productService.getProductsFiltered(keyword, categoryId, page, size))
            .build();
    }

  
    // 2. CREATE (Tạo mới) - ADMIN
    @PostMapping
    public ApiResponse<Product> createProduct(@RequestBody @Valid ProductCreateRequest request) {
        return ApiResponse.<Product>builder()
            .result(productService.createProduct(request))
            .build();
    }


    // 3. READ (Lấy chi tiết) - CÔNG KHAI
    @GetMapping("/{id}")
    public ApiResponse<Product> getProductDetail(@PathVariable Long id) {
        return ApiResponse.<Product>builder()
            .result(productService.getProductById(id))
            .build();
    }


    // 4. UPDATE (Cập nhật) - ADMIN
    @PutMapping("/{id}")
    public ApiResponse<Product> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateRequest request) {
        return ApiResponse.<Product>builder()
            .result(productService.updateProduct(id, request))
            .build();
    }


    // 5. DELETE (Xóa) - ADMIN
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.<Void>builder().build(); // Trả về mã 204 No Content
    }
}