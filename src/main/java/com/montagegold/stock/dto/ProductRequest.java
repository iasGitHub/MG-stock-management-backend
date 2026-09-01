package com.montagegold.stock.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "The reference is required")
    @Size(max = 50, message = "The reference must not exceed 50 characters")
    private String reference;

    @NotBlank(message = "The product name is required")
    @Size(max = 100, message = "The name must not exceed 100 characters")
    private String name;

    @Size(max = 255, message = "The description must not exceed 255 characters")
    private String description;

    @NotBlank(message = "The category is required")
    @Size(max = 50, message = "The category must not exceed 50 characters")
    private String category;

    @NotNull(message = "The minimum threshold is required")
    @Min(value = 0, message = "The minimum threshold cannot be negative")
    private Integer minThreshold;

    @NotNull(message = "The unit price is required")
    @DecimalMin(value = "0.01", message = "The price must be greater than 0")
    private Double unitPrice;

    @Min(value = 0, message = "The initial quantity cannot be negative")
    private Integer initialQuantity = 0;
}
