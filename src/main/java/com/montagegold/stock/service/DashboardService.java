package com.montagegold.stock.service;

import com.montagegold.stock.dto.DashboardStats;
import com.montagegold.stock.dto.ProduitResponse;
import com.montagegold.stock.enums.TypeMouvement;
import com.montagegold.stock.repository.MouvementStockRepository;
import com.montagegold.stock.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProduitRepository produitRepository;
    private final MouvementStockRepository mouvementRepository;

    public DashboardStats getStats() {
        LocalDateTime debutMois = LocalDateTime.now().withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);

        List<com.montagegold.stock.entity.Produit> produits = produitRepository.findAll();

        int quantiteTotale = produits.stream()
                .mapToInt(com.montagegold.stock.entity.Produit::getQuantiteStock)
                .sum();

        double valeurStock = produits.stream()
                .mapToDouble(p -> p.getQuantiteStock() * p.getPrixUnitaire())
                .sum();

        return DashboardStats.builder()
                .totalProduits(produits.size())
                .produitsEnAlerte(produitRepository.countProduitsEnAlerte())
                .quantiteTotale(quantiteTotale)
                .valeurStock(valeurStock)
                .entreesDuMois(mouvementRepository.countByTypeDepuis(TypeMouvement.ENTREE, debutMois))
                .sortiesDuMois(mouvementRepository.countByTypeDepuis(TypeMouvement.SORTIE, debutMois))
                .build();
    }

    public List<ProduitResponse> getProduitsEnAlerte() {
        return produitRepository.findProduitsEnAlerte().stream()
                .map(p -> ProduitResponse.builder()
                        .id(p.getId())
                        .reference(p.getReference())
                        .nom(p.getNom())
                        .description(p.getDescription())
                        .categorie(p.getCategorie())
                        .quantiteStock(p.getQuantiteStock())
                        .seuilMin(p.getSeuilMin())
                        .prixUnitaire(p.getPrixUnitaire())
                        .enAlerte(true)
                        .dateCreation(p.getDateCreation())
                        .dateModification(p.getDateModification())
                        .build())
                .collect(Collectors.toList());
    }
}
