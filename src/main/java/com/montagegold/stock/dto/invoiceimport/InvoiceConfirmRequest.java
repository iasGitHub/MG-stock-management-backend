package com.montagegold.stock.dto.invoiceimport;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceConfirmRequest {

    private String supplierNif;

    @NotBlank(message = "The supplier name is required")
    private String supplierName;

    private String supplierPhone;

    private String supplierAddress;

    private String invoiceNumber;

    private String invoiceDate;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<InvoiceConfirmItem> items;
}
