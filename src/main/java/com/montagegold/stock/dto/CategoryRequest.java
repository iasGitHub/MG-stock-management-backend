package com.montagegold.stock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {

    @NotBlank(message = "Le nom de la catégorie est requis")
    @Size(max = 50, message = "Le nom de la catégorie ne doit pas dépasser 50 caractères")
    private String name;
}
