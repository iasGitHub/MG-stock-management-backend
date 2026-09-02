package com.montagegold.stock.controller;

import com.montagegold.stock.dto.StockMovementRequest;
import com.montagegold.stock.dto.StockMovementResponse;
import com.montagegold.stock.enums.MovementType;
import com.montagegold.stock.service.ExcelService;
import com.montagegold.stock.service.StockMovementService;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;
    private final ExcelService excelService;

    @GetMapping
    public ResponseEntity<Page<StockMovementResponse>> findAll(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) MovementType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("movementDate").descending());
        return ResponseEntity.ok(stockMovementService.findAll(productId, type, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<StockMovementResponse> record(@Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockMovementService.record(request));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public void exportExcel(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) MovementType type,
            HttpServletResponse response) throws IOException {

        Page<StockMovementResponse> page = stockMovementService.findAll(
                productId, type, PageRequest.of(0, 10000, Sort.by("movementDate").descending()));
        List<StockMovementResponse> movements = page.getContent();
        excelService.exportMovements(movements, response);
    }
}
