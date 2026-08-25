package com.montagegold.stock.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ProduitResponse {

    private Long id;
    private String reference;
    private String nom;
    private String description;
    private String categorie;
    private Integer quantiteStock;
    private Integer seuilMin;
    private Double prixUnitaire;
    private boolean enAlerte;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
