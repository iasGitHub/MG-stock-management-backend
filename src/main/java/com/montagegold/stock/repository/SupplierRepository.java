package com.montagegold.stock.repository;

import com.montagegold.stock.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    boolean existsByNifIgnoreCase(String code);

    Optional<Supplier> findByNifIgnoreCase(String code);

    Optional<Supplier> findByNifIgnoreCaseAndIdNot(String code, Long id);

    Page<Supplier> findByNameContainingIgnoreCaseOrNifContainingIgnoreCase(
            String name, String code, Pageable pageable);
}
