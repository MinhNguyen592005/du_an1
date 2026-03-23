package com.vu.springapi.controller;

import com.vu.springapi.dto.request.ChangePasswordRequest;
import com.vu.springapi.dto.request.UpdateMyInfoRequest;
import com.vu.springapi.dto.request.UserCreateRequest;
import com.vu.springapi.dto.request.UserUpdateRequest;
import com.vu.springapi.dto.response.ApiResponse;
import com.vu.springapi.dto.response.UserResponse;
import com.vu.springapi.model.User;
import com.vu.springapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreateRequest userCreateRequest){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.createUser(userCreateRequest));
        return apiResponse;
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getUsers(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Username: {}", authentication.getName());
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));

        ApiResponse<List<UserResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getUsers());
        return apiResponse;
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable("id") Long id){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getUser(id));
        return apiResponse;
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(@PathVariable("id") Long id, @RequestBody @Valid UserUpdateRequest userUpdateRequest){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.updateUser(id, userUpdateRequest));
        return apiResponse;
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteUser(@PathVariable("id") Long id){
        ApiResponse<String> apiResponse = new ApiResponse<>();
        userService.deleteUser(id);
        apiResponse.setResult("user has been deleted!");
        return apiResponse;
    }

    @GetMapping("/myinfo")
    public ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @PutMapping("/updatemyinfo")
    public ApiResponse<UserResponse> updateMyInfo(@RequestBody @Valid UpdateMyInfoRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateMyInfo(request))
                .build();
    }
    @PutMapping("/changePassword/{id}")
    public ApiResponse<UserResponse> changePassword(@PathVariable("id") Long id ,@RequestBody @Valid ChangePasswordRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.changePassword(id, request))
                .build();
    }
    @PutMapping("/changePassword")
    public ApiResponse<UserResponse> changePassword(@RequestBody @Valid ChangePasswordRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.changePassword(request))
                .build();
    }
}
