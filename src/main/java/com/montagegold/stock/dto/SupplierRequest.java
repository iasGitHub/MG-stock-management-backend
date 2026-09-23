package com.montagegold.stock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierRequest {

    @NotBlank(message = "Le NIF est requis")
    @Size(max = 50, message = "Le NIF ne doit pas dépasser 50 caractères")
    private String nif;

    @NotBlank(message = "Le nom du fournisseur est requis")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String name;

    @Size(max = 20, message = "Le téléphone ne doit pas dépasser 20 caractères")
    private String phone;

    @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
    private String address;
}
