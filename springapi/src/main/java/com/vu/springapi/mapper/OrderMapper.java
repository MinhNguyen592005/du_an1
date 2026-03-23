package com.vu.springapi.mapper;

import com.vu.springapi.dto.request.OrderCreateRequest;
import com.vu.springapi.dto.request.OrderUpdateRequest;
import com.vu.springapi.dto.response.OrderItemResponse;
import com.vu.springapi.dto.response.OrderResponse;
import com.vu.springapi.model.Order;
import com.vu.springapi.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {AddressMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "placedAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    Order toOrder(OrderCreateRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.username")
    @Mapping(target = "addressId", source = "address.id")
    @Mapping(target = "address", source = "address")
    OrderResponse toOrderResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "shippingFee", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "placedAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    void updateOrder(@MappingTarget Order order, OrderUpdateRequest request);
}

