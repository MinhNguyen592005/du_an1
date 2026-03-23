package com.vu.springapi.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {

    @NotNull(message = "ID san pham khong duoc de trong")
    @Positive(message = "ID san pham phai lon hon 0")
    private Long productId;

    @NotNull(message = "So luong khong duoc de trong")
    @Positive(message = "So luong phai lon hon 0")
    private Integer quantity;
}

