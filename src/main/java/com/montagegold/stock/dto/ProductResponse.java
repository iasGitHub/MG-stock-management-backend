package com.montagegold.stock.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ProductResponse {

    private Long id;
    private String reference;
    private String name;
    private String description;
    private String category;
    private Integer stockQuantity;
    private Integer minThreshold;
    private Double unitPrice;
    private boolean inAlert;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
