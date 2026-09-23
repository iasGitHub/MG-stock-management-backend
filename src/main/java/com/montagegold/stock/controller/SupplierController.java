package com.montagegold.stock.controller;

import com.montagegold.stock.dto.SupplierLiteResponse;
import com.montagegold.stock.dto.SupplierRequest;
import com.montagegold.stock.dto.SupplierResponse;
import com.montagegold.stock.service.ExcelService;
import com.montagegold.stock.service.SupplierService;
import com.montagegold.stock.util.PageableFactory;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;
    private final ExcelService excelService;

    @GetMapping
    public ResponseEntity<Page<SupplierResponse>> findAll(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Pageable pageable = PageableFactory.of(page, size, sortBy, sortDir, "name",
                Set.of("name", "nif", "createdDate"));
        return ResponseEntity.ok(supplierService.findAll(search, pageable));
    }

    @GetMapping("/lite")
    public ResponseEntity<List<SupplierLiteResponse>> findAllLite() {
        return ResponseEntity.ok(supplierService.findAllLite());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<SupplierResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(supplierService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        List<SupplierRequest> parsed = excelService.parseSupplierImport(file);
        int created = 0;
        int skipped = 0;
        for (SupplierRequest req : parsed) {
            try {
                supplierService.create(req);
                created++;
            } catch (Exception e) {
                skipped++;
            }
        }
        return ResponseEntity.ok(Map.of(
                "created", created,
                "skipped", skipped,
                "total", parsed.size()
        ));
    }

    @GetMapping("/export/template")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public void exportTemplate(HttpServletResponse response) throws IOException {
        excelService.exportSupplierTemplate(response);
    }
}
