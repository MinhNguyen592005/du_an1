package com.vu.springapi.mapper;

import com.vu.springapi.dto.request.UpdateMyInfoRequest;
import com.vu.springapi.dto.request.UserCreateRequest;
import com.vu.springapi.dto.request.UserUpdateRequest;
import com.vu.springapi.dto.response.UserResponse;
import com.vu.springapi.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreateRequest request);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
    UserResponse toUserResponse(User user);
    void updateMyInfo(@MappingTarget User user, UpdateMyInfoRequest request);
}
