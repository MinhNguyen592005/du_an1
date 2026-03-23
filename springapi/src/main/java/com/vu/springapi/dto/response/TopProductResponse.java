package com.vu.springapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopProductResponse {
    private Long productId;
    private String productName;
    private Long quantitySold;
    private String productImage;
}

