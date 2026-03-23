package com.vu.springapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import lombok.*;

import java.util.Date;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder

public class InvalidatedToken {
    @Id
    private String id;
    private Date expiryTime;
}
