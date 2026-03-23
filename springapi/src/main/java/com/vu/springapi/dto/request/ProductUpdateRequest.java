package com.vu.springapi.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {
    private String name;
    private Long categoryId;
    private String description;
    private BigDecimal price;
    private Integer stock; 
    private String imageUrl;
    private Boolean isActive;
}