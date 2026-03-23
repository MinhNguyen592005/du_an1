package com.vu.springapi.controller;

import com.nimbusds.jose.JOSEException;
import com.vu.springapi.dto.request.AuthenticationRequest;
import com.vu.springapi.dto.request.ForgetPasswordRequest;
import com.vu.springapi.dto.request.IntrospectRequest;
import com.vu.springapi.dto.request.LogoutRequest;
import com.vu.springapi.dto.request.ResetPasswordRequest;
import com.vu.springapi.dto.request.VerifyOtpRequest;
import com.vu.springapi.dto.response.ApiResponse;
import com.vu.springapi.dto.response.AuthenticationResponse;
import com.vu.springapi.dto.response.ForgetPasswordResponse;
import com.vu.springapi.dto.response.IntrospectResponse;
import com.vu.springapi.dto.response.ResetPasswordResponse;
import com.vu.springapi.dto.response.VerifyOtpResponse;
import com.vu.springapi.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }
    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }
    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .build();
    }

    @PostMapping("/forget-password")
    ApiResponse<ForgetPasswordResponse> forgetPassword(@Valid @RequestBody ForgetPasswordRequest request){
        var result = authenticationService.forgetPassword(request);
        return ApiResponse.<ForgetPasswordResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/verify-otp")
    ApiResponse<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request){
        var result = authenticationService.verifyOtp(request);
        return ApiResponse.<VerifyOtpResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/reset-password")
    ApiResponse<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request){
        var result = authenticationService.resetPassword(request);
        return ApiResponse.<ResetPasswordResponse>builder()
                .result(result)
                .build();
    }
}
