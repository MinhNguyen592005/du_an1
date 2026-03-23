package com.vu.springapi.service;

import com.vu.springapi.dto.request.PermissionRequest;
import com.vu.springapi.dto.response.PermissionResponse;
import com.vu.springapi.exception.AppException;
import com.vu.springapi.exception.ErrorCode;
import com.vu.springapi.mapper.PermissionMapper;
import com.vu.springapi.model.Permission;
import com.vu.springapi.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public PermissionResponse create(PermissionRequest request){
        Permission permission = permissionMapper.toPermission(request);
        permissionRepository.save(permission);

        return permissionMapper.toPermissionResponse(permission);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<PermissionResponse> getAll(){
        var permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PermissionResponse update(String name, PermissionRequest request){
        Permission permission = permissionRepository.findById(name)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));

        permissionMapper.updatePermission(permission, request);
        permissionRepository.save(permission);

        return permissionMapper.toPermissionResponse(permission);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(String permission){
        permissionRepository.deleteById(permission);
    }
}
