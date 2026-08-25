package com.montagegold.stock.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La référence est obligatoire")
    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @NotBlank(message = "Le nom du produit est obligatoire")
    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 255)
    private String description;

    @NotNull(message = "La catégorie est obligatoire")
    @Column(nullable = false, length = 50)
    private String categorie;

    @Builder.Default
    @Min(value = 0, message = "La quantité ne peut pas être négative")
    @Column(nullable = false)
    private Integer quantiteStock = 0;

    @NotNull(message = "Le seuil minimum est obligatoire")
    @Min(value = 0, message = "Le seuil minimum ne peut pas être négatif")
    @Column(nullable = false)
    private Integer seuilMin;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être supérieur à 0")
    @Column(nullable = false)
    private Double prixUnitaire;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @Column
    private LocalDateTime dateModification;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }
}
