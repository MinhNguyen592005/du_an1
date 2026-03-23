package com.vu.springapi.service;

import com.vu.springapi.dto.request.RoleRequest;
import com.vu.springapi.dto.response.RoleResponse;
import com.vu.springapi.exception.AppException;
import com.vu.springapi.exception.ErrorCode;
import com.vu.springapi.mapper.RoleMapper;
import com.vu.springapi.model.Role;
import com.vu.springapi.repository.PermissionRepository;
import com.vu.springapi.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionRepository permissionRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse create(RoleRequest request){
        Role role = roleMapper.toRole(request);
        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));

        roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<RoleResponse> getAll(){
        var roles = roleRepository.findAll();
        return roles.stream().map(roleMapper::toRoleResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(String role){
        roleRepository.deleteById(role);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse update(String curRole, RoleRequest request){
        Role role = roleRepository.findById(curRole).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        roleMapper.updateRole(role, request);

        // Only update permissions if provided and not empty
        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
            var permissions = permissionRepository.findAllById(request.getPermissions());
            role.setPermissions(new HashSet<>(permissions));
        }

        return roleMapper.toRoleResponse(roleRepository.save(role));
    }
}
