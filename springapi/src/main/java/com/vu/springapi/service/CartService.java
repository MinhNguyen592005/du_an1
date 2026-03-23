package com.vu.springapi.service;

import com.vu.springapi.dto.request.AddToCartRequest;
import com.vu.springapi.dto.request.UpdateCartItemRequest;
import com.vu.springapi.dto.response.CartResponse;
import com.vu.springapi.exception.AppException;
import com.vu.springapi.exception.ErrorCode;
import com.vu.springapi.mapper.CartItemMapper;
import com.vu.springapi.mapper.CartMapper;
import com.vu.springapi.model.*;
import com.vu.springapi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Autowired
    private CartItemMapper cartItemMapper;

    public CartResponse getCartByUserId(Long userId) {
        // Tìm cart theo userId, nếu không có thì tạo mới
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setCreatedAt(LocalDateTime.now());
                    newCart.setUpdatedAt(LocalDateTime.now());
                    return cartRepository.save(newCart);
                });

        CartResponse response = cartMapper.toCartResponse(cart);

        // Populate cartItems
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        response.setCartItems(cartItemMapper.toCartItemResponseList(items));

        // Calculate totals
        calculateCartTotals(response, items);

        return response;
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    public CartResponse addItemToCart(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setCreatedAt(LocalDateTime.now());
                    newCart.setUpdatedAt(LocalDateTime.now());
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (product.getStock() < quantity) {
            throw new RuntimeException("Tồn kho không đủ");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            if (product.getStock() < newQuantity) {
                throw new RuntimeException("Tồn kho không đủ cho số lượng được cập nhật");
            }

            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setAddedAt(LocalDateTime.now());
            cartItemRepository.save(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return getCartByUserId(userId);
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng (THEO ITEM ID)
     */
    public CartResponse removeItemFromCart(Long userId, Long itemId) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền xóa sản phẩm này");
        }

        Cart cart = cartItem.getCart();
        cartItemRepository.delete(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        log.info("Xóa sản phẩm {} khỏi giỏ hàng", itemId);
        return getCartByUserId(userId);
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        cartItemRepository.deleteByCart(cart);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
        log.info("Xóa toàn bộ giỏ hàng của user: {}", userId);
    }

    /**
     * Thêm sản phẩm vào giỏ hàng (dùng DTO)
     */
    public CartResponse addItemToCart(Long userId, AddToCartRequest request) {
        return addItemToCart(userId, request.getProductId(), request.getQuantity());
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng (THEO ITEM ID)
     */
    public CartResponse updateItemQuantity(Long userId, Long itemId, UpdateCartItemRequest request) {
        if (request.getQuantity() <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền cập nhật sản phẩm này");
        }

        Cart cart = cartItem.getCart();
        Product product = cartItem.getProduct();

        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Tồn kho không đủ cho số lượng được yêu cầu");
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        log.info("Cập nhật số lượng sản phẩm {} của user: {}", itemId, userId);
        return getCartByUserId(userId);
    }

    /**
     * Tính toán tổng số lượng và tổng tiền từ danh sách CartItem
     */
    private void calculateCartTotals(CartResponse response, List<CartItem> cartItems) {
        if (cartItems != null && !cartItems.isEmpty()) {
            int totalItems = 0;
            BigDecimal totalPrice = BigDecimal.ZERO;

            for (CartItem item : cartItems) {
                totalItems += item.getQuantity();

                BigDecimal itemPrice = item.getProduct().getPrice();
                BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                totalPrice = totalPrice.add(itemTotal);
            }

            response.setTotalItems(totalItems);
            response.setTotalPrice(totalPrice);
        } else {
            response.setTotalItems(0);
            response.setTotalPrice(BigDecimal.ZERO);
        }
    }
}