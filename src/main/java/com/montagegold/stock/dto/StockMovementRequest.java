package com.montagegold.stock.dto;

import com.montagegold.stock.enums.MovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockMovementRequest {

    @NotNull(message = "Le produit est requis")
    private Long productId;

    @NotNull(message = "Le type de mouvement est requis")
    private MovementType type;

    @NotNull(message = "La quantité est requise")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantity;

    @Size(max = 255, message = "Le motif ne doit pas dépasser 255 caractères")
    private String reason;

    @Size(max = 100, message = "La référence externe ne doit pas dépasser 100 caractères")
    private String externalReference;

    @Size(max = 150, message = "Le destinataire ne doit pas dépasser 150 caractères")
    private String recipient;

    private Long supplierId;

    @DecimalMin(value = "0.01", message = "Le prix unitaire doit être supérieur à 0")
    private Double unitPrice;
}
