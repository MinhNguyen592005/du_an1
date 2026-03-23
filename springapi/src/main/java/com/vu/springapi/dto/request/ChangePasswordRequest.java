package com.vu.springapi.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    @Size(min = 8, message = "PASSWORD_INVALID")
    private String currentPassword;

    @Size(min = 8, message = "PASSWORD_INVALID")
    private String newPassword;
}
