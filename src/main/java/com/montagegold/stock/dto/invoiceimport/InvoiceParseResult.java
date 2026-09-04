package com.montagegold.stock.dto.invoiceimport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceParseResult {

    @Builder.Default
    private List<InvoiceDraft> invoices = new ArrayList<>();

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    private int parsed;

    private int failed;

    public void addInvoice(InvoiceDraft draft) {
        this.invoices.add(draft);
    }

    public void addError(String error) {
        this.errors.add(error);
    }
}
