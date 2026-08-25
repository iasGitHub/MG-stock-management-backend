package com.montagegold.stock.repository;

import com.montagegold.stock.entity.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProduitRepository extends JpaRepository<Produit, Long> {

    boolean existsByReference(String reference);

    Optional<Produit> findByReferenceAndIdNot(String reference, Long id);

    Page<Produit> findByNomContainingIgnoreCaseOrReferenceContainingIgnoreCase(
            String nom, String reference, Pageable pageable);

    List<Produit> findByCategorieIgnoreCase(String categorie);

    @Query("SELECT p FROM Produit p WHERE p.quantiteStock <= p.seuilMin")
    List<Produit> findProduitsEnAlerte();

    @Query("SELECT COUNT(p) FROM Produit p WHERE p.quantiteStock <= p.seuilMin")
    long countProduitsEnAlerte();

    @Query("SELECT DISTINCT p.categorie FROM Produit p ORDER BY p.categorie")
    List<String> findAllCategories();
}
