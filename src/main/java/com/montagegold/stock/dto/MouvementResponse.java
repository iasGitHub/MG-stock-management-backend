package com.montagegold.stock.dto;

import com.montagegold.stock.enums.TypeMouvement;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MouvementResponse {

    private Long id;
    private Long produitId;
    private String produitNom;
    private String produitReference;
    private TypeMouvement type;
    private Integer quantite;
    private String motif;
    private String referenceExterne;
    private Long fournisseurId;
    private String fournisseurCode;
    private String fournisseurNom;
    private String destinataire;
    private Double prixUnitaire;
    private String utilisateurNom;
    private LocalDateTime dateMouvement;
}
