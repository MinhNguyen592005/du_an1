package com.vu.springapi.controller;

import com.vu.springapi.dto.request.AddressCreateRequest;
import com.vu.springapi.dto.request.AddressUpdateRequest;
import com.vu.springapi.dto.response.AddressResponse;
import com.vu.springapi.dto.response.ApiResponse;
import com.vu.springapi.service.AddressService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@Slf4j
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ApiResponse<List<AddressResponse>> getAllAddresses() {
        log.info("API: Lay tat ca dia chi");
        return ApiResponse.<List<AddressResponse>>builder()
                .result(addressService.getAllAddresses())
                .build();
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<AddressResponse>> getAddressesByUserId(
            @PathVariable @Positive Long userId) {
        log.info("API: Lay dia chi cua user: {}", userId);
        return ApiResponse.<List<AddressResponse>>builder()
                .result(addressService.getAddressesByUserId(userId))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<AddressResponse> getAddressById(@PathVariable @Positive Long id) {
        log.info("API: Lay dia chi ID: {}", id);
        return ApiResponse.<AddressResponse>builder()
                .result(addressService.getAddressById(id))
                .build();
    }

    @PostMapping("/user/{userId}")
    public ApiResponse<AddressResponse> createAddress(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody AddressCreateRequest request) {
        log.info("API: Tao dia chi moi cho user: {}", userId);
        return ApiResponse.<AddressResponse>builder()
                .result(addressService.createAddress(userId, request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<AddressResponse> updateAddress(
            @PathVariable @Positive Long id,
            @Valid @RequestBody AddressUpdateRequest request) {
        log.info("API: Cap nhat dia chi ID: {}", id);
        return ApiResponse.<AddressResponse>builder()
                .result(addressService.updateAddress(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteAddress(@PathVariable @Positive Long id) {
        log.info("API: Xoa dia chi ID: {}", id);
        addressService.deleteAddress(id);
        return ApiResponse.<String>builder()
                .result("Dia chi da duoc xoa thanh cong")
                .build();
    }
}

