package com.montagegold.stock.repository;

import com.montagegold.stock.entity.Fournisseur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<Fournisseur> findByCodeIgnoreCaseAndIdNot(String code, Long id);

    Page<Fournisseur> findByNomContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String nom, String code, Pageable pageable);
}
