package com.montagegold.stock.service;

import com.montagegold.stock.config.InvoiceImportProperties;
import com.montagegold.stock.dto.ProductRequest;
import com.montagegold.stock.dto.SupplierRequest;
import com.montagegold.stock.dto.StockMovementRequest;
import com.montagegold.stock.dto.invoiceimport.InvoiceConfirmItem;
import com.montagegold.stock.dto.invoiceimport.InvoiceConfirmRequest;
import com.montagegold.stock.dto.invoiceimport.InvoiceDraft;
import com.montagegold.stock.dto.invoiceimport.InvoiceImportResult;
import com.montagegold.stock.dto.invoiceimport.InvoiceItemDraft;
import com.montagegold.stock.dto.invoiceimport.InvoiceParseResult;
import com.montagegold.stock.entity.Product;
import com.montagegold.stock.entity.Supplier;
import com.montagegold.stock.enums.MovementType;
import com.montagegold.stock.repository.ProductRepository;
import com.montagegold.stock.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceImportService {

    private static final String DEFAULT_CATEGORY = "Général";

    private final PdfTextExtractor pdfTextExtractor;
    private final LlmInvoiceParser llmInvoiceParser;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final SupplierService supplierService;
    private final StockMovementService stockMovementService;
    private final InvoiceImportProperties properties;

    /**
     * Phase 1 - parse uploaded PDF files into editable drafts. Nothing is written
     * to the database here.
     */
    public InvoiceParseResult parsePdfs(List<MultipartFile> files) {
        InvoiceParseResult result = InvoiceParseResult.builder().build();

        if (!properties.isEnabled()) {
            result.addError("PDF invoice import is disabled. Set INVOICE_IMPORT_ENABLED=true to enable it.");
            return result;
        }

        if (files == null || files.isEmpty()) {
            result.addError("No file provided");
            return result;
        }

        for (MultipartFile file : files) {
            String name = file.getOriginalFilename();
            try {
                String text = pdfTextExtractor.extract(file.getInputStream());
                InvoiceDraft draft = llmInvoiceParser.parse(name, text);
                result.addInvoice(draft);
                result.setParsed(result.getParsed() + 1);
            } catch (Exception e) {
                result.addError(name + ": " + e.getMessage());
                result.setFailed(result.getFailed() + 1);
            }
        }
        return result;
    }

    /**
     * Phase 2 - persist validated drafts: create missing suppliers and products,
     * then record stock-IN movements.
     */
    public InvoiceImportResult confirmImport(List<InvoiceConfirmRequest> invoices) {
        InvoiceImportResult result = InvoiceImportResult.builder().build();

        if (invoices == null || invoices.isEmpty()) {
            result.getErrors().add("No invoice data provided");
            return result;
        }

        for (InvoiceConfirmRequest invoice : invoices) {
            try {
                importOne(invoice, result);
            } catch (Exception e) {
                result.getErrors().add(
                        (invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() + ": " : "")
                                + e.getMessage());
            }
        }
        return result;
    }

    private void importOne(InvoiceConfirmRequest invoice, InvoiceImportResult result) {
        Supplier supplier = resolveSupplier(invoice, result);
        if (supplier == null) {
            result.getErrors().add("Supplier not found and could not be created: " + invoice.getSupplierName());
            return;
        }

        for (InvoiceConfirmItem item : invoice.getItems()) {
            try {
                Product product = resolveProduct(item, result);
                StockMovementRequest movementReq = new StockMovementRequest();
                movementReq.setProductId(product.getId());
                movementReq.setType(MovementType.IN);
                movementReq.setQuantity(item.getQuantity());
                movementReq.setReason("Import facture " + invoice.getInvoiceNumber());
                movementReq.setExternalReference(invoice.getInvoiceNumber());
                movementReq.setSupplierId(supplier.getId());
                movementReq.setUnitPrice(item.getUnitPrice());
                stockMovementService.record(movementReq);
                result.setMovementsCreated(result.getMovementsCreated() + 1);
            } catch (Exception e) {
                result.getErrors().add(item.getProductName() + ": " + e.getMessage());
            }
        }
    }

    private Supplier resolveSupplier(InvoiceConfirmRequest invoice, InvoiceImportResult result) {
        String nif = invoice.getSupplierNif();
        if (nif != null && !nif.isBlank()) {
            Supplier existing = supplierRepository.findByNifIgnoreCase(nif.trim()).orElse(null);
            if (existing != null) return existing;
        }
        try {
            SupplierRequest req = new SupplierRequest();
            req.setNif(nif != null && !nif.isBlank()
                    ? nif.trim()
                    : "FRS-" + System.currentTimeMillis());
            req.setName(invoice.getSupplierName().trim());
            req.setPhone(blankToNull(invoice.getSupplierPhone()));
            req.setAddress(blankToNull(invoice.getSupplierAddress()));
            var created = supplierService.create(req);
            result.setSuppliersCreated(result.getSuppliersCreated() + 1);
            return supplierRepository.findById(created.getId()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private Product resolveProduct(InvoiceConfirmItem item, InvoiceImportResult result) {
        String name = item.getProductName().trim();
        Product existing = productRepository.findByNameIgnoreCase(name).orElse(null);
        if (existing != null) return existing;

        ProductRequest req = new ProductRequest();
        req.setName(name);
        req.setReference("generated");
        req.setCategory(blankToNull(item.getCategory()) != null
                ? item.getCategory().trim()
                : DEFAULT_CATEGORY);
        req.setMinThreshold(0);
        req.setUnitPrice(item.getUnitPrice() != null ? item.getUnitPrice() : 0.01);
        req.setInitialQuantity(0);
        var created = productService.create(req);
        result.setProductsCreated(result.getProductsCreated() + 1);
        return productRepository.findById(created.getId()).orElseThrow();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
