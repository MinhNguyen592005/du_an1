package com.vu.springapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    @NotNull(message = "ID nguoi dung khong duoc de trong")
    @Positive(message = "ID nguoi dung phai lon hon 0")
    private Long userId;

    @NotNull(message = "ID dia chi khong duoc de trong")
    @Positive(message = "ID dia chi phai lon hon 0")
    private Long addressId;

    @NotEmpty(message = "Danh sach san pham khong duoc rong")
    @Valid
    private List<OrderItemRequest> items;

    private BigDecimal shippingFee;

    private String note;
}

