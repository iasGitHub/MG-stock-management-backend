package com.montagegold.stock.controller;

import com.montagegold.stock.dto.auth.UtilisateurRequest;
import com.montagegold.stock.dto.auth.UtilisateurResponse;
import com.montagegold.stock.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    public ResponseEntity<List<UtilisateurResponse>> findAll() {
        return ResponseEntity.ok(utilisateurService.findAll());
    }

    @PostMapping
    public ResponseEntity<UtilisateurResponse> create(@Valid @RequestBody UtilisateurRequest request) {
        return ResponseEntity.ok(utilisateurService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody UtilisateurRequest request) {
        return ResponseEntity.ok(utilisateurService.update(id, request));
    }

    @PatchMapping("/{id}/toggle-actif")
    public ResponseEntity<Void> toggleActif(@PathVariable Long id) {
        utilisateurService.toggleActif(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        utilisateurService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
