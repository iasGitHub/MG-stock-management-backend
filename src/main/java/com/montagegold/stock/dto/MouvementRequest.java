package com.montagegold.stock.dto;

import com.montagegold.stock.enums.TypeMouvement;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MouvementRequest {

    @NotNull(message = "Le produit est obligatoire")
    private Long produitId;

    @NotNull(message = "Le type de mouvement est obligatoire")
    private TypeMouvement type;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantite;

    @Size(max = 255, message = "Le motif ne doit pas dépasser 255 caractères")
    private String motif;

    @Size(max = 100, message = "La référence externe ne doit pas dépasser 100 caractères")
    private String referenceExterne;

    @Size(max = 150, message = "Le destinataire ne doit pas dépasser 150 caractères")
    private String destinataire;

    private Long fournisseurId;

    private Double prixUnitaire;
}
