package com.montagegold.stock.controller;

import com.montagegold.stock.dto.invoiceimport.InvoiceConfirmRequest;
import com.montagegold.stock.dto.invoiceimport.InvoiceImportResult;
import com.montagegold.stock.dto.invoiceimport.InvoiceParseResult;
import com.montagegold.stock.service.InvoiceImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceImportController {

    private final InvoiceImportService invoiceImportService;

    /**
     * Phase 1 - upload PDFs, get structured drafts back for review (nothing saved).
     */
    @PostMapping("/parse")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<InvoiceParseResult> parse(
            @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(invoiceImportService.parsePdfs(files));
    }

    /**
     * Phase 2 - confirm reviewed/edited drafts to persist suppliers,
     * products and stock-IN movements.
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<InvoiceImportResult> confirm(
            @Valid @RequestBody List<InvoiceConfirmRequest> invoices) {
        return ResponseEntity.ok(invoiceImportService.confirmImport(invoices));
    }
}
