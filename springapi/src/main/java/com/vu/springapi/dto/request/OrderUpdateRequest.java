package com.vu.springapi.dto.request;

import jakarta.validation.Valid;
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
public class OrderUpdateRequest {

    @Positive(message = "ID dia chi phai lon hon 0")
    private Long addressId;

    @Valid
    private List<OrderItemRequest> items;

    private BigDecimal shippingFee;

    private String status;

    private String note;
}

