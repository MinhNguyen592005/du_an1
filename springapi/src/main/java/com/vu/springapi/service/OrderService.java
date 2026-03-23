package com.vu.springapi.service;

import com.vu.springapi.dto.request.OrderCreateRequest;
import com.vu.springapi.dto.request.OrderItemRequest;
import com.vu.springapi.dto.request.OrderUpdateRequest;
import com.vu.springapi.dto.response.OrderResponse;
import com.vu.springapi.exception.AppException;
import com.vu.springapi.exception.ErrorCode;
import com.vu.springapi.mapper.OrderMapper;
import com.vu.springapi.model.Order;
import com.vu.springapi.model.OrderItem;
import com.vu.springapi.model.Product;
import com.vu.springapi.model.User;
import com.vu.springapi.model.Address;
import com.vu.springapi.repository.OrderRepository;
import com.vu.springapi.repository.ProductRepository;
import com.vu.springapi.repository.UserRepository;
import com.vu.springapi.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        log.info("Lay tat ca don hang voi phan trang");
        return orderRepository.findAll(pageable)
                .map(orderMapper::toOrderResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable) {
        log.info("Lay tat ca don hang cua user: {}", userId);
        return orderRepository.findByUser_Id(userId, pageable)
                .map(orderMapper::toOrderResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        log.info("Lay don hang theo ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toOrderResponse(order);
    }

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        log.info("Tao don hang moi cho user: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        Order order = orderMapper.toOrder(request);
        order.setUser(user);
        order.setAddress(address);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus("pending");
        // Lưu thời gian theo múi giờ Việt Nam (UTC+7)
        order.setPlacedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

        if (request.getShippingFee() != null) {
            order.setShippingFee(request.getShippingFee());
        } else {
            order.setShippingFee(BigDecimal.ZERO);
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .unitPrice(product.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())))
                    .build();

            orderItems.add(orderItem);
            subtotal = subtotal.add(orderItem.getTotalPrice());
        }

        order.setItems(orderItems);
        order.setSubtotal(subtotal);
        order.setTotal(subtotal.add(order.getShippingFee()));

        Order savedOrder = orderRepository.save(order);
        log.info("Da tao don hang moi: {}", savedOrder.getOrderNumber());

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Transactional
    public OrderResponse updateOrder(Long id, OrderUpdateRequest request) {
        log.info("Cap nhat don hang ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Update address if provided
        if (request.getAddressId() != null) {
            Address address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
            order.setAddress(address);
        }

        // Update items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            // Clear existing items
            order.getItems().clear();

            // Add new items and recalculate subtotal
            BigDecimal subtotal = BigDecimal.ZERO;

            for (OrderItemRequest itemRequest : request.getItems()) {
                Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .productName(product.getName())
                        .unitPrice(product.getPrice())
                        .quantity(itemRequest.getQuantity())
                        .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())))
                        .build();

                order.getItems().add(orderItem);
                subtotal = subtotal.add(orderItem.getTotalPrice());
            }

            order.setSubtotal(subtotal);

            // Update shipping fee if provided
            if (request.getShippingFee() != null) {
                order.setShippingFee(request.getShippingFee());
            }

            // Recalculate total
            order.setTotal(order.getSubtotal().add(order.getShippingFee()));
        } else {
            // If only shipping fee is updated without changing items
            if (request.getShippingFee() != null) {
                order.setShippingFee(request.getShippingFee());
                order.setTotal(order.getSubtotal().add(order.getShippingFee()));
            }
        }

        // Update status and note using mapper
        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
        }

        if (request.getNote() != null) {
            order.setNote(request.getNote());
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Da cap nhat don hang ID: {}", id);

        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Transactional
    public void deleteOrder(Long id) {
        log.info("Xoa don hang ID: {}", id);

        if (!orderRepository.existsById(id)) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        orderRepository.deleteById(id);
        log.info("Da xoa don hang ID: {}", id);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

