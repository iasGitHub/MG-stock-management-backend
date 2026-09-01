package com.montagegold.stock.dto;

import com.montagegold.stock.enums.MovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockMovementRequest {

    @NotNull(message = "The product is required")
    private Long productId;

    @NotNull(message = "The movement type is required")
    private MovementType type;

    @NotNull(message = "The quantity is required")
    @Min(value = 1, message = "The quantity must be at least 1")
    private Integer quantity;

    @Size(max = 255, message = "The reason must not exceed 255 characters")
    private String reason;

    @Size(max = 100, message = "The external reference must not exceed 100 characters")
    private String externalReference;

    @Size(max = 150, message = "The recipient must not exceed 150 characters")
    private String recipient;

    private Long supplierId;

    private Double unitPrice;
}
