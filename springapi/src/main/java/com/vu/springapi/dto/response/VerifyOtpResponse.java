package com.vu.springapi.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyOtpResponse {
    private boolean valid;
}
