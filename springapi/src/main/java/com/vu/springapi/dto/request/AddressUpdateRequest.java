package com.vu.springapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressUpdateRequest {

    private String label;

    private String street;

    private String city;

    private String state;

    private String postalCode;

    private String country;
}

