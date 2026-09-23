package com.montagegold.stock.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "La référence est requise")
    @Size(max = 50, message = "La référence ne doit pas dépasser 50 caractères")
    private String reference;

    @NotBlank(message = "Le nom du produit est requis")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String name;

    @Size(max = 255, message = "La description ne doit pas dépasser 255 caractères")
    private String description;

    @NotBlank(message = "La catégorie est requise")
    @Size(max = 50, message = "La catégorie ne doit pas dépasser 50 caractères")
    private String category;

    @NotNull(message = "Le seuil minimum est requis")
    @Min(value = 0, message = "Le seuil minimum ne peut pas être négatif")
    private Integer minThreshold;

    @NotNull(message = "Le prix unitaire est requis")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    private Double unitPrice;

    @Min(value = 0, message = "La quantité initiale ne peut pas être négative")
    private Integer initialQuantity = 0;
}
