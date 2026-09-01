package com.montagegold.stock.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "The reference is required")
    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @NotBlank(message = "The product name is required")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @NotNull(message = "The category is required")
    @Column(nullable = false, length = 50)
    private String category;

    @Builder.Default
    @Min(value = 0, message = "The quantity cannot be negative")
    @Column(nullable = false)
    private Integer stockQuantity = 0;

    @NotNull(message = "The minimum threshold is required")
    @Min(value = 0, message = "The minimum threshold cannot be negative")
    @Column(nullable = false)
    private Integer minThreshold;

    @NotNull(message = "The unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "The price must be greater than 0")
    @Column(nullable = false)
    private Double unitPrice;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @Column
    private LocalDateTime updatedDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
