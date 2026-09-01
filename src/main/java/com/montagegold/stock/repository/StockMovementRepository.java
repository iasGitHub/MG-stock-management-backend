package com.montagegold.stock.repository;

import com.montagegold.stock.entity.StockMovement;
import com.montagegold.stock.enums.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    Page<StockMovement> findByProductId(Long productId, Pageable pageable);

    Page<StockMovement> findByProductIdAndType(Long productId, MovementType type, Pageable pageable);

    Page<StockMovement> findByType(MovementType type, Pageable pageable);

    boolean existsBySupplierId(Long supplierId);

    @Query("SELECT m FROM StockMovement m WHERE m.movementDate BETWEEN :start AND :end ORDER BY m.movementDate DESC")
    List<StockMovement> findByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(m) FROM StockMovement m WHERE m.type = :type AND m.movementDate >= :start")
    long countByTypeSince(@Param("type") MovementType type, @Param("start") LocalDateTime start);
}
