package com.montagegold.stock.controller;

import com.montagegold.stock.dto.ProductLiteResponse;
import com.montagegold.stock.dto.ProductRequest;
import com.montagegold.stock.dto.ProductResponse;
import com.montagegold.stock.service.ExcelService;
import com.montagegold.stock.service.ProductService;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ExcelService excelService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> findAll(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        // Tri restreint a une liste blanche, taille bornee (cf. PageableFactory).
        Pageable pageable = PageableFactory.of(page, size, sortBy, sortDir, "name",
                Set.of("name", "reference", "unitPrice", "stockQuantity", "minThreshold", "createdDate"));
        return ResponseEntity.ok(productService.findAll(search, pageable));
    }

    @GetMapping("/next-reference")
    public ResponseEntity<Map<String, String>> nextReference() {
        return ResponseEntity.ok(Map.of("reference", productService.nextReference()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lite")
    public ResponseEntity<List<ProductLiteResponse>> findAllLite() {
        return ResponseEntity.ok(productService.findAllLite());
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        List<ProductRequest> parsed = excelService.parseProductImport(file);
        int created = 0;
        int skipped = 0;
        for (ProductRequest req : parsed) {
            try {
                productService.createFromImport(req);
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
        excelService.exportProductTemplate(response);
    }
}
