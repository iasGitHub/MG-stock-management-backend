package com.montagegold.stock.controller;

import com.montagegold.stock.dto.ProduitRequest;
import com.montagegold.stock.dto.ProduitResponse;
import com.montagegold.stock.service.DashboardService;
import com.montagegold.stock.service.ProduitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitService produitService;
    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<Page<ProduitResponse>> findAll(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nom") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(produitService.findAll(search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProduitResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<ProduitResponse> create(@Valid @RequestBody ProduitRequest request) {
        return ResponseEntity.ok(produitService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<ProduitResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody ProduitRequest request) {
        return ResponseEntity.ok(produitService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        produitService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alertes")
    public ResponseEntity<List<ProduitResponse>> produitsEnAlerte() {
        return ResponseEntity.ok(dashboardService.getProduitsEnAlerte());
    }
}
