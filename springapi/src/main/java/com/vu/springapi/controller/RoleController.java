package com.vu.springapi.controller;

import com.vu.springapi.dto.request.RoleRequest;
import com.vu.springapi.dto.response.ApiResponse;
import com.vu.springapi.dto.response.RoleResponse;
import com.vu.springapi.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    @PostMapping
    public ApiResponse<RoleResponse> create(@RequestBody RoleRequest request){
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<RoleResponse>> getAll(){
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }

    @DeleteMapping("/{role}")
    public ApiResponse<Void> delete(@PathVariable String role){
        roleService.delete(role);
        return ApiResponse.<Void>builder().build();
    }


    @PutMapping("/{role}")
    public ApiResponse<RoleResponse> update(@PathVariable("role") String curRole, @RequestBody RoleRequest request){
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.update(curRole, request))
                .build();
    }
}
