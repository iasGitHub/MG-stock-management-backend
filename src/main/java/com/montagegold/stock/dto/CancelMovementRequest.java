package com.montagegold.stock.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelMovementRequest {

    @Size(max = 255, message = "The reason must not exceed 255 characters")
    private String reason;
}