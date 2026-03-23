package com.vu.springapi.dto.response;


import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String name;
    private String username;
    private String email;
    private String phone;
    private Set<RoleResponse> roles;
    private BigDecimal totalOrderValue;
}
