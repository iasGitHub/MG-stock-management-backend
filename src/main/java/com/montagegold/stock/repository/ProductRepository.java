package com.montagegold.stock.repository;

import com.montagegold.stock.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByReference(String reference);

    Optional<Product> findByReferenceAndIdNot(String reference, Long id);

    boolean existsByCategoryId(Long categoryId);

    Page<Product> findByNameContainingIgnoreCaseOrReferenceContainingIgnoreCase(
            String name, String reference, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.minThreshold")
    List<Product> findProductsInAlert();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity <= p.minThreshold")
    long countProductsInAlert();

    @Query("SELECT p.reference FROM Product p")
    List<String> findAllReferences();
}
