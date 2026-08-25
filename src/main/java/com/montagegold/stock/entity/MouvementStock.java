package com.montagegold.stock.entity;

import com.montagegold.stock.enums.TypeMouvement;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mouvements_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouvementStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @NotNull(message = "Le type de mouvement est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TypeMouvement type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    @Size(max = 150, message = "Le destinataire ne doit pas dépasser 150 caractères")
    @Column(length = 150)
    private String destinataire;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    @Column(nullable = false)
    private Integer quantite;

    @Column(length = 255)
    private String motif;

    @Column(length = 100)
    private String referenceExterne;

    @Column(nullable = false)
    private Double prixUnitaire;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false)
    private LocalDateTime dateMouvement;

    @PrePersist
    public void prePersist() {
        this.dateMouvement = LocalDateTime.now();
    }
}
