package com.vu.springapi.mapper;

import com.vu.springapi.dto.request.PermissionRequest;
import com.vu.springapi.dto.request.UserCreateRequest;
import com.vu.springapi.dto.request.UserUpdateRequest;
import com.vu.springapi.dto.response.PermissionResponse;
import com.vu.springapi.dto.response.UserResponse;
import com.vu.springapi.model.Permission;
import com.vu.springapi.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
    void updatePermission(@MappingTarget Permission permission, PermissionRequest request);
}
