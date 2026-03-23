package com.vu.springapi.controller;

import com.vu.springapi.dto.request.AddToCartRequest;
import com.vu.springapi.dto.request.UpdateCartItemRequest;
import com.vu.springapi.dto.response.ApiResponse;
import com.vu.springapi.dto.response.CartResponse;
import com.vu.springapi.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    /**
     * Lấy giỏ hàng của user
     */
    @GetMapping("/{userId}")
    public ApiResponse<CartResponse> getCart(@PathVariable @Positive Long userId) {
        log.info("Lấy giỏ hàng của user: {}", userId);
        return ApiResponse.<CartResponse>builder()
                .result(cartService.getCartByUserId(userId))
                .build();
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @PostMapping("/{userId}/items")
    public ApiResponse<CartResponse> addItemToCart(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody AddToCartRequest request) {
        
        log.info("Thêm sản phẩm vào giỏ hàng của user: {}", userId);
        return ApiResponse.<CartResponse>builder()
                .result(cartService.addItemToCart(userId, request))
                .build();
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    @PutMapping("/{userId}/items/{itemId}")
    public ApiResponse<CartResponse> updateItemQuantity(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        
        log.info("Cập nhật số lượng sản phẩm trong giỏ hàng của user: {}, item: {}", userId, itemId);
        return ApiResponse.<CartResponse>builder()
                .result(cartService.updateItemQuantity(userId, itemId, request))
                .build();
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng (THEO ITEM ID)
     */
    @DeleteMapping("/{userId}/items/{itemId}")
    public ApiResponse<CartResponse> removeItemFromCart(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long itemId) {
        
        log.info("Xóa sản phẩm khỏi giỏ hàng của user: {}, item: {}", userId, itemId);
        return ApiResponse.<CartResponse>builder()
                .result(cartService.removeItemFromCart(userId, itemId))
                .build();
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    @DeleteMapping("/{userId}/clear")
    public ApiResponse<String> clearCart(@PathVariable @Positive Long userId) {
        log.info("Xóa toàn bộ giỏ hàng của user: {}", userId);
        cartService.clearCart(userId);
        return ApiResponse.<String>builder()
                .result("Giỏ hàng đã được xóa thành công")
                .build();
    }

}