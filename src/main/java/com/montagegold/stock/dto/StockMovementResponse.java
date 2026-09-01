package com.montagegold.stock.dto;

import com.montagegold.stock.enums.MovementType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class StockMovementResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productReference;
    private MovementType type;
    private Integer quantity;
    private String reason;
    private String externalReference;
    private Long supplierId;
    private String supplierNif;
    private String supplierName;
    private String recipient;
    private Double unitPrice;
    private String userName;
    private LocalDateTime movementDate;
}
