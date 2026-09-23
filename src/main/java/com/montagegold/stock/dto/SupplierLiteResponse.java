package com.montagegold.stock.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Vue allégée pour les listes déroulantes : uniquement les champs affichés,
 * sans pagination.
 */
@Getter
@Setter
@Builder
public class SupplierLiteResponse {

    private Long id;
    private String nif;
    private String name;
}
