package com.vu.springapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String sku; 

    @NotNull(message = "ID danh mục không được để trống")
    private Long categoryId;

    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @PositiveOrZero(message = "Giá không thể âm")
    private BigDecimal price;

    @PositiveOrZero(message = "Số lượng tồn kho không thể âm")
    private int stock;

    private String imageUrl;
}