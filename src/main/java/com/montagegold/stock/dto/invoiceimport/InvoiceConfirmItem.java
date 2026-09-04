package com.montagegold.stock.dto.invoiceimport;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceConfirmItem {

    @NotBlank(message = "The product name is required")
    private String productName;

    private String category;

    @NotNull(message = "The quantity is required")
    @Min(value = 1, message = "The quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "The unit price is required")
    @Min(value = 0, message = "The unit price cannot be negative")
    private Double unitPrice;
}
