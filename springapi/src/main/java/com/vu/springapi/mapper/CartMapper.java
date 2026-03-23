package com.vu.springapi.mapper;

import com.vu.springapi.dto.response.CartResponse;
import com.vu.springapi.dto.response.CartItemResponse;
import com.vu.springapi.model.Cart;
import com.vu.springapi.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {
    
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "cartItems", ignore = true) 
    @Mapping(target = "totalItems", ignore = true) 
    @Mapping(target = "totalPrice", ignore = true) 
    CartResponse toCartResponse(Cart cart);
    
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImage", source = "product.imageUrl")
    @Mapping(target = "productPrice", source = "product.price")
    @Mapping(target = "totalPrice", ignore = true) // Tính sau
    CartItemResponse toCartItemResponse(CartItem cartItem);
    
    List<CartItemResponse> toCartItemResponseList(List<CartItem> cartItems);
}