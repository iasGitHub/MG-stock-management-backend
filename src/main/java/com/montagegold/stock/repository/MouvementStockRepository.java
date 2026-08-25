package com.montagegold.stock.repository;

import com.montagegold.stock.entity.MouvementStock;
import com.montagegold.stock.enums.TypeMouvement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {

    Page<MouvementStock> findByProduitId(Long produitId, Pageable pageable);

    Page<MouvementStock> findByProduitIdAndType(Long produitId, TypeMouvement type, Pageable pageable);

    Page<MouvementStock> findByType(TypeMouvement type, Pageable pageable);

    boolean existsByFournisseurId(Long fournisseurId);

    @Query("SELECT m FROM MouvementStock m WHERE m.dateMouvement BETWEEN :debut AND :fin ORDER BY m.dateMouvement DESC")
    List<MouvementStock> findByPeriode(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(m) FROM MouvementStock m WHERE m.type = :type AND m.dateMouvement >= :debut")
    long countByTypeDepuis(@Param("type") TypeMouvement type, @Param("debut") LocalDateTime debut);
}
