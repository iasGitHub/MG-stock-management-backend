package com.montagegold.stock.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProduitRequest {

    @NotBlank(message = "La référence est obligatoire")
    @Size(max = 50, message = "La référence ne doit pas dépasser 50 caractères")
    private String reference;

    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @Size(max = 255, message = "La description ne doit pas dépasser 255 caractères")
    private String description;

    @NotBlank(message = "La catégorie est obligatoire")
    @Size(max = 50, message = "La catégorie ne doit pas dépasser 50 caractères")
    private String categorie;

    @NotNull(message = "Le seuil minimum est obligatoire")
    @Min(value = 0, message = "Le seuil minimum ne peut pas être négatif")
    private Integer seuilMin;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    private Double prixUnitaire;

    @Min(value = 0, message = "La quantité initiale ne peut pas être négative")
    private Integer quantiteInitiale = 0;
}
