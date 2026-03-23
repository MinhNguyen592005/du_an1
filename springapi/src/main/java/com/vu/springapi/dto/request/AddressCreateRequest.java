package com.vu.springapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressCreateRequest {

    @NotBlank(message = "Label khong duoc de trong")
    private String label;

    @NotBlank(message = "Dia chi duong khong duoc de trong")
    private String street;

    @NotBlank(message = "Thanh pho khong duoc de trong")
    private String city;

    private String state;

    private String postalCode;

    private String country;
}

