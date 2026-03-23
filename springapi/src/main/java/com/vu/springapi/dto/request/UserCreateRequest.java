package com.vu.springapi.dto.request;

import com.vu.springapi.exception.ErrorCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    private String name;

    private String username;

    @Size(min = 8, message = "PASSWORD_INVALID")
    private String password;

    private String email;

    private String phone;

    private LocalDate dob;

}
