package com.montagegold.stock.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelMovementRequest {

    @Size(max = 255, message = "Le motif ne doit pas dépasser 255 caractères")
    private String reason;
}
