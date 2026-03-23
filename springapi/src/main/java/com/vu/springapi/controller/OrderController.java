package com.vu.springapi.controller;

import com.vu.springapi.dto.request.OrderCreateRequest;
import com.vu.springapi.dto.request.OrderUpdateRequest;
import com.vu.springapi.dto.response.ApiResponse;
import com.vu.springapi.dto.response.OrderResponse;
import com.vu.springapi.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "placedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        log.info("API: Lay tat ca don hang - page: {}, size: {}", page, size);

        Sort sort = direction.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ApiResponse.<Page<OrderResponse>>builder()
                .result(orderService.getAllOrders(pageable))
                .build();
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<Page<OrderResponse>> getOrdersByUserId(
            @PathVariable @Positive Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "placedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        log.info("API: Lay don hang cua user: {}", userId);

        Sort sort = direction.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ApiResponse.<Page<OrderResponse>>builder()
                .result(orderService.getOrdersByUserId(userId, pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable @Positive Long id) {
        log.info("API: Lay don hang ID: {}", id);
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getOrderById(id))
                .build();
    }

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        log.info("API: Tao don hang moi");
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.createOrder(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<OrderResponse> updateOrder(
            @PathVariable @Positive Long id,
            @Valid @RequestBody OrderUpdateRequest request) {
        log.info("API: Cap nhat don hang ID: {}", id);
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.updateOrder(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteOrder(@PathVariable @Positive Long id) {
        log.info("API: Xoa don hang ID: {}", id);
        orderService.deleteOrder(id);
        return ApiResponse.<String>builder()
                .result("Don hang da duoc xoa thanh cong")
                .build();
    }
}

