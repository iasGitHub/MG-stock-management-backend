package com.montagegold.stock.service;

import com.montagegold.stock.dto.ProduitRequest;
import com.montagegold.stock.dto.ProduitResponse;
import com.montagegold.stock.entity.Produit;
import com.montagegold.stock.exception.BusinessException;
import com.montagegold.stock.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProduitService {

    private final ProduitRepository produitRepository;

    public Page<ProduitResponse> findAll(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return produitRepository
                    .findByNomContainingIgnoreCaseOrReferenceContainingIgnoreCase(search, search, pageable)
                    .map(this::toResponse);
        }
        return produitRepository.findAll(pageable).map(this::toResponse);
    }

    public List<Produit> findAllList() {
        return produitRepository.findAll();
    }

    public ProduitResponse findById(Long id) {
        return produitRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> notFound(id));
    }

    @Transactional
    public ProduitResponse create(ProduitRequest request) {
        if (produitRepository.existsByReference(request.getReference())) {
            throw new BusinessException("Cette référence produit existe déjà", HttpStatus.CONFLICT);
        }
        Produit produit = Produit.builder()
                .reference(request.getReference())
                .nom(request.getNom())
                .description(request.getDescription())
                .categorie(request.getCategorie())
                .quantiteStock(request.getQuantiteInitiale() != null ? request.getQuantiteInitiale() : 0)
                .seuilMin(request.getSeuilMin())
                .prixUnitaire(request.getPrixUnitaire())
                .build();
        return toResponse(produitRepository.save(produit));
    }

    @Transactional
    public ProduitResponse update(Long id, ProduitRequest request) {
        Produit produit = produitRepository.findById(id).orElseThrow(() -> notFound(id));

        produitRepository.findByReferenceAndIdNot(request.getReference(), id)
                .ifPresent(p -> {
                    throw new BusinessException("Cette référence produit existe déjà", HttpStatus.CONFLICT);
                });

        produit.setReference(request.getReference());
        produit.setNom(request.getNom());
        produit.setDescription(request.getDescription());
        produit.setCategorie(request.getCategorie());
        produit.setSeuilMin(request.getSeuilMin());
        produit.setPrixUnitaire(request.getPrixUnitaire());
        return toResponse(produitRepository.save(produit));
    }

    @Transactional
    public void delete(Long id) {
        if (!produitRepository.existsById(id)) {
            throw notFound(id);
        }
        produitRepository.deleteById(id);
    }

    private BusinessException notFound(Long id) {
        return new BusinessException("Produit introuvable (id=" + id + ")", HttpStatus.NOT_FOUND);
    }

    private ProduitResponse toResponse(Produit p) {
        return ProduitResponse.builder()
                .id(p.getId())
                .reference(p.getReference())
                .nom(p.getNom())
                .description(p.getDescription())
                .categorie(p.getCategorie())
                .quantiteStock(p.getQuantiteStock())
                .seuilMin(p.getSeuilMin())
                .prixUnitaire(p.getPrixUnitaire())
                .enAlerte(p.getQuantiteStock() <= p.getSeuilMin())
                .dateCreation(p.getDateCreation())
                .dateModification(p.getDateModification())
                .build();
    }
}
