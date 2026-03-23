package com.vu.springapi.exception;

import com.vu.springapi.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(Exception exception){
        exception.printStackTrace();
        ApiResponse apiResponse = new ApiResponse();
        if (exception instanceof org.springframework.web.servlet.resource.NoResourceFoundException) {
            apiResponse.setCode(ErrorCode.UNCATEGORIZE_EXCEPTION.getCode());
            apiResponse.setMessage("Tài nguyên không tìm thấy. Kiểm tra URL hoặc Route.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }
         apiResponse.setCode(ErrorCode.UNCATEGORIZE_EXCEPTION.getCode());
         apiResponse.setMessage(ErrorCode.UNCATEGORIZE_EXCEPTION.getMessage());
         return ResponseEntity.status(ErrorCode.UNCATEGORIZE_EXCEPTION.getStatusCode()).body(apiResponse);
    }


    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException exception){
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }


    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingValidation(MethodArgumentNotValidException exception){
        String enumkey = exception.getFieldError().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        try {
            errorCode = ErrorCode.valueOf(enumkey);
        } catch (IllegalArgumentException e){

        }
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AuthorizationDeniedException.class)
    ResponseEntity<ApiResponse> handlingAuthorizationDeniedException(AuthorizationDeniedException exception){
        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiResponse> handlingHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException exception){
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(1015);
        apiResponse.setMessage("Content-Type not supported. Please use 'application/json'");
        return ResponseEntity.badRequest().body(apiResponse);
    }
}

