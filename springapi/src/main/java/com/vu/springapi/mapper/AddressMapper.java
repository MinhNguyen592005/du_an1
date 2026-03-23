package com.vu.springapi.mapper;

import com.vu.springapi.dto.request.AddressCreateRequest;
import com.vu.springapi.dto.request.AddressUpdateRequest;
import com.vu.springapi.dto.response.AddressResponse;
import com.vu.springapi.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AddressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Address toAddress(AddressCreateRequest request);

    @Mapping(target = "userId", source = "user.id")
    AddressResponse toAddressResponse(Address address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateAddress(@MappingTarget Address address, AddressUpdateRequest request);
}

