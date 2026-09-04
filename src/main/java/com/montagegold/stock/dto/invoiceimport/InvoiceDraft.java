package com.montagegold.stock.dto.invoiceimport;

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
public class InvoiceDraft {

    private String fileName;

    private String supplierNif;

    private String supplierName;

    private String supplierPhone;

    private String supplierAddress;

    private String invoiceNumber;

    private String invoiceDate;

    private List<InvoiceItemDraft> items;
}
