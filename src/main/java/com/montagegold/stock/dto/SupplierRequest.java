package com.montagegold.stock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierRequest {

    @NotBlank(message = "The nif is required")
    @Size(max = 50, message = "The nif must not exceed 50 characters")
    private String nif;

    @NotBlank(message = "The supplier name is required")
    @Size(max = 100, message = "The name must not exceed 100 characters")
    private String name;

    @Size(max = 20, message = "The phone must not exceed 20 characters")
    private String phone;

    @Size(max = 255, message = "The address must not exceed 255 characters")
    private String address;
}
