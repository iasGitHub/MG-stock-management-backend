package com.montagegold.stock.service;

import com.montagegold.stock.dto.MouvementRequest;
import com.montagegold.stock.dto.MouvementResponse;
import com.montagegold.stock.entity.Fournisseur;
import com.montagegold.stock.entity.MouvementStock;
import com.montagegold.stock.entity.Produit;
import com.montagegold.stock.entity.Utilisateur;
import com.montagegold.stock.enums.TypeMouvement;
import com.montagegold.stock.exception.BusinessException;
import com.montagegold.stock.repository.FournisseurRepository;
import com.montagegold.stock.repository.MouvementStockRepository;
import com.montagegold.stock.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MouvementStockService {

    private final MouvementStockRepository mouvementRepository;
    private final ProduitRepository produitRepository;
    private final FournisseurRepository fournisseurRepository;
    private final UtilisateurService utilisateurService;

    public Page<MouvementResponse> findAll(Long produitId, TypeMouvement type, Pageable pageable) {
        Page<MouvementStock> page;
        if (produitId != null && type != null) {
            page = mouvementRepository.findByProduitIdAndType(produitId, type, pageable);
        } else if (produitId != null) {
            page = mouvementRepository.findByProduitId(produitId, pageable);
        } else if (type != null) {
            page = mouvementRepository.findByType(type, pageable);
        } else {
            page = mouvementRepository.findAll(pageable);
        }
        return page.map(this::toResponse);
    }

    @Transactional
    public MouvementResponse enregistrer(MouvementRequest request) {
        Produit produit = produitRepository.findById(request.getProduitId())
                .orElseThrow(() -> new BusinessException(
                        "Produit introuvable (id=" + request.getProduitId() + ")", HttpStatus.NOT_FOUND));

        String username = currentUsername();
        Utilisateur utilisateur = utilisateurService.getByUsername(username);

        int nouvelleQuantite;
        if (request.getType() == TypeMouvement.ENTREE) {
            nouvelleQuantite = produit.getQuantiteStock() + request.getQuantite();
        } else {
            nouvelleQuantite = produit.getQuantiteStock() - request.getQuantite();
            if (nouvelleQuantite < 0) {
                throw new BusinessException(String.format(
                        "Stock insuffisant pour '%s' : disponible=%d, demandé=%d",
                        produit.getNom(), produit.getQuantiteStock(), request.getQuantite()),
                        HttpStatus.BAD_REQUEST);
            }
        }

        produit.setQuantiteStock(nouvelleQuantite);

        Double prix = request.getPrixUnitaire() != null ? request.getPrixUnitaire() : produit.getPrixUnitaire();

        Fournisseur fournisseur = null;
        String destinataire = null;
        if (request.getType() == TypeMouvement.ENTREE) {
            if (request.getFournisseurId() == null) {
                throw new BusinessException("Le fournisseur est obligatoire pour une entrée de stock",
                        HttpStatus.BAD_REQUEST);
            }
            fournisseur = fournisseurRepository.findById(request.getFournisseurId())
                    .orElseThrow(() -> new BusinessException(
                            "Fournisseur introuvable (id=" + request.getFournisseurId() + ")",
                            HttpStatus.NOT_FOUND));
        } else {
            if (request.getDestinataire() == null || request.getDestinataire().isBlank()) {
                throw new BusinessException("Le destinataire est obligatoire pour une sortie de stock",
                        HttpStatus.BAD_REQUEST);
            }
            destinataire = request.getDestinataire().trim();
        }

        MouvementStock mouvement = MouvementStock.builder()
                .produit(produit)
                .type(request.getType())
                .quantite(request.getQuantite())
                .motif(request.getMotif())
                .referenceExterne(request.getReferenceExterne())
                .fournisseur(fournisseur)
                .destinataire(destinataire)
                .prixUnitaire(prix)
                .utilisateur(utilisateur)
                .build();

        return toResponse(mouvementRepository.save(mouvement));
    }

    private String currentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return principal.toString();
    }

    private MouvementResponse toResponse(MouvementStock m) {
        return MouvementResponse.builder()
                .id(m.getId())
                .produitId(m.getProduit().getId())
                .produitNom(m.getProduit().getNom())
                .produitReference(m.getProduit().getReference())
                .type(m.getType())
                .quantite(m.getQuantite())
                .motif(m.getMotif())
                .referenceExterne(m.getReferenceExterne())
                .fournisseurId(m.getFournisseur() != null ? m.getFournisseur().getId() : null)
                .fournisseurCode(m.getFournisseur() != null ? m.getFournisseur().getCode() : null)
                .fournisseurNom(m.getFournisseur() != null ? m.getFournisseur().getNom() : null)
                .destinataire(m.getDestinataire())
                .prixUnitaire(m.getPrixUnitaire())
                .utilisateurNom(m.getUtilisateur().getNomComplet())
                .dateMouvement(m.getDateMouvement())
                .build();
    }
}
