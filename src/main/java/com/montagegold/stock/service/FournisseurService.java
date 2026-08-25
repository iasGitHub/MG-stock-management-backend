package com.montagegold.stock.service;

import com.montagegold.stock.dto.FournisseurRequest;
import com.montagegold.stock.dto.FournisseurResponse;
import com.montagegold.stock.entity.Fournisseur;
import com.montagegold.stock.exception.BusinessException;
import com.montagegold.stock.repository.FournisseurRepository;
import com.montagegold.stock.repository.MouvementStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FournisseurService {

    private final FournisseurRepository fournisseurRepository;
    private final MouvementStockRepository mouvementStockRepository;

    public Page<FournisseurResponse> findAll(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return fournisseurRepository
                    .findByNomContainingIgnoreCaseOrCodeContainingIgnoreCase(search, search, pageable)
                    .map(this::toResponse);
        }
        return fournisseurRepository.findAll(pageable).map(this::toResponse);
    }

    public List<Fournisseur> findAllList() {
        return fournisseurRepository.findAll();
    }

    public Fournisseur getById(Long id) {
        return fournisseurRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    public FournisseurResponse findById(Long id) {
        return toResponse(getById(id));
    }

    @Transactional
    public FournisseurResponse create(FournisseurRequest request) {
        if (fournisseurRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new BusinessException("Ce code fournisseur existe déjà", HttpStatus.CONFLICT);
        }
        return toResponse(fournisseurRepository.save(toEntity(new Fournisseur(), request)));
    }

    @Transactional
    public FournisseurResponse update(Long id, FournisseurRequest request) {
        Fournisseur fournisseur = getById(id);

        fournisseurRepository.findByCodeIgnoreCaseAndIdNot(request.getCode(), id)
                .ifPresent(f -> {
                    throw new BusinessException("Ce code fournisseur existe déjà", HttpStatus.CONFLICT);
                });

        toEntity(fournisseur, request);
        return toResponse(fournisseurRepository.save(fournisseur));
    }

    @Transactional
    public void delete(Long id) {
        if (!fournisseurRepository.existsById(id)) {
            throw notFound(id);
        }
        if (mouvementStockRepository.existsByFournisseurId(id)) {
            throw new BusinessException(
                    "Impossible de supprimer ce fournisseur : des mouvements y sont associés",
                    HttpStatus.CONFLICT);
        }
        fournisseurRepository.deleteById(id);
    }

    private Fournisseur toEntity(Fournisseur fournisseur, FournisseurRequest request) {
        fournisseur.setCode(request.getCode());
        fournisseur.setNom(request.getNom());
        fournisseur.setTelephone(request.getTelephone());
        fournisseur.setAdresse(request.getAdresse());
        return fournisseur;
    }

    private BusinessException notFound(Long id) {
        return new BusinessException("Fournisseur introuvable (id=" + id + ")", HttpStatus.NOT_FOUND);
    }

    private FournisseurResponse toResponse(Fournisseur f) {
        return FournisseurResponse.builder()
                .id(f.getId())
                .code(f.getCode())
                .nom(f.getNom())
                .telephone(f.getTelephone())
                .adresse(f.getAdresse())
                .dateCreation(f.getDateCreation())
                .build();
    }
}
