package com.montagegold.stock.controller;

import com.montagegold.stock.dto.MouvementRequest;
import com.montagegold.stock.dto.MouvementResponse;
import com.montagegold.stock.enums.TypeMouvement;
import com.montagegold.stock.service.MouvementStockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mouvements")
@RequiredArgsConstructor
public class MouvementStockController {

    private final MouvementStockService mouvementStockService;

    @GetMapping
    public ResponseEntity<Page<MouvementResponse>> findAll(
            @RequestParam(required = false) Long produitId,
            @RequestParam(required = false) TypeMouvement type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("dateMouvement").descending());
        return ResponseEntity.ok(mouvementStockService.findAll(produitId, type, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<MouvementResponse> enregistrer(@Valid @RequestBody MouvementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mouvementStockService.enregistrer(request));
    }
}
