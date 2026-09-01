package com.montagegold.stock.service;

import com.montagegold.stock.dto.DashboardStats;
import com.montagegold.stock.dto.ProductResponse;
import com.montagegold.stock.entity.Product;
import com.montagegold.stock.enums.MovementType;
import com.montagegold.stock.repository.StockMovementRepository;
import com.montagegold.stock.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;
    private final StockMovementRepository movementRepository;

    public DashboardStats getStats() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);

        List<Product> products = productRepository.findAll();

        int totalQuantity = products.stream()
                .mapToInt(Product::getStockQuantity)
                .sum();

        double stockValue = products.stream()
                .mapToDouble(p -> p.getStockQuantity() * p.getUnitPrice())
                .sum();

        return DashboardStats.builder()
                .totalProducts(products.size())
                .productsInAlert(productRepository.countProductsInAlert())
                .totalQuantity(totalQuantity)
                .stockValue(stockValue)
                .monthlyEntries(movementRepository.countByTypeSince(MovementType.IN, startOfMonth))
                .monthlyExits(movementRepository.countByTypeSince(MovementType.OUT, startOfMonth))
                .build();
    }

    public List<ProductResponse> getProductsInAlert() {
        return productRepository.findProductsInAlert().stream()
                .map(p -> ProductResponse.builder()
                        .id(p.getId())
                        .reference(p.getReference())
                        .name(p.getName())
                        .description(p.getDescription())
                        .category(p.getCategory())
                        .stockQuantity(p.getStockQuantity())
                        .minThreshold(p.getMinThreshold())
                        .unitPrice(p.getUnitPrice())
                        .inAlert(true)
                        .createdDate(p.getCreatedDate())
                        .updatedDate(p.getUpdatedDate())
                        .build())
                .collect(Collectors.toList());
    }
}
