package com.vu.springapi.service;

import com.vu.springapi.dto.request.AddressCreateRequest;
import com.vu.springapi.dto.request.AddressUpdateRequest;
import com.vu.springapi.dto.response.AddressResponse;
import com.vu.springapi.exception.AppException;
import com.vu.springapi.exception.ErrorCode;
import com.vu.springapi.mapper.AddressMapper;
import com.vu.springapi.model.Address;
import com.vu.springapi.model.User;
import com.vu.springapi.repository.AddressRepository;
import com.vu.springapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Transactional(readOnly = true)
    public List<AddressResponse> getAllAddresses() {
        log.info("Lay tat ca dia chi");
        return addressRepository.findAll().stream()
                .map(addressMapper::toAddressResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getAddressesByUserId(Long userId) {
        log.info("Lay tat ca dia chi cua user: {}", userId);
        return addressRepository.findByUserId(userId).stream()
                .map(addressMapper::toAddressResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long id) {
        log.info("Lay dia chi theo ID: {}", id);
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        return addressMapper.toAddressResponse(address);
    }

    @Transactional
    public AddressResponse createAddress(Long userId, AddressCreateRequest request) {
        log.info("Tao dia chi moi cho user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Address address = addressMapper.toAddress(request);
        address.setUser(user);

        if (address.getCountry() == null || address.getCountry().isEmpty()) {
            address.setCountry("Vietnam");
        }

        Address savedAddress = addressRepository.save(address);
        log.info("Da tao dia chi moi voi ID: {}", savedAddress.getId());

        return addressMapper.toAddressResponse(savedAddress);
    }

    @Transactional
    public AddressResponse updateAddress(Long id, AddressUpdateRequest request) {
        log.info("Cap nhat dia chi ID: {}", id);

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        addressMapper.updateAddress(address, request);

        Address updatedAddress = addressRepository.save(address);
        log.info("Da cap nhat dia chi ID: {}", id);

        return addressMapper.toAddressResponse(updatedAddress);
    }

    @Transactional
    public void deleteAddress(Long id) {
        log.info("Xoa dia chi ID: {}", id);

        if (!addressRepository.existsById(id)) {
            throw new AppException(ErrorCode.ADDRESS_NOT_FOUND);
        }

        addressRepository.deleteById(id);
        log.info("Da xoa dia chi ID: {}", id);
    }
}

