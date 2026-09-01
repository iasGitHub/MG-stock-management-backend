package com.montagegold.stock.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SupplierResponse {

    private Long id;
    private String nif;
    private String name;
    private String phone;
    private String address;
    private LocalDateTime createdDate;
}
