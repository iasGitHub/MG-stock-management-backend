package com.montagegold.stock.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Vue allégée pour les listes déroulantes (filtres, formulaires) :
 * uniquement les champs affichés, sans pagination.
 */
@Getter
@Setter
@Builder
public class ProductLiteResponse {

    private Long id;
    private String reference;
    private String name;
    private Integer stockQuantity;
}
