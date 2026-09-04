package com.montagegold.stock.repository;

import com.montagegold.stock.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByReference(String reference);

    Optional<Product> findByNameIgnoreCase(String name);

    Optional<Product> findByNameIgnoreCaseAndIdNot(String name, Long id);

    Optional<Product> findByReferenceAndIdNot(String reference, Long id);

    Page<Product> findByNameContainingIgnoreCaseOrReferenceContainingIgnoreCase(
            String name, String reference, Pageable pageable);

    List<Product> findByCategoryIgnoreCase(String category);

    boolean existsByCategoryIgnoreCase(String category);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.minThreshold")
    List<Product> findProductsInAlert();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity <= p.minThreshold")
    long countProductsInAlert();

    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findAllCategories();

    @Query("SELECT p.reference FROM Product p")
    List<String> findAllReferences();
}
