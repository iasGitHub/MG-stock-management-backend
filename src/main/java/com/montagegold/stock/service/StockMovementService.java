package com.montagegold.stock.service;

import com.montagegold.stock.dto.StockMovementRequest;
import com.montagegold.stock.dto.StockMovementResponse;
import com.montagegold.stock.entity.Supplier;
import com.montagegold.stock.entity.StockMovement;
import com.montagegold.stock.entity.Product;
import com.montagegold.stock.entity.User;
import com.montagegold.stock.enums.MovementType;
import com.montagegold.stock.exception.BusinessException;
import com.montagegold.stock.repository.SupplierRepository;
import com.montagegold.stock.repository.StockMovementRepository;
import com.montagegold.stock.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final UserService userService;

    public Page<StockMovementResponse> findAll(Long productId, MovementType type, Pageable pageable) {
        Page<StockMovement> page;
        if (productId != null && type != null) {
            page = movementRepository.findByProductIdAndType(productId, type, pageable);
        } else if (productId != null) {
            page = movementRepository.findByProductId(productId, pageable);
        } else if (type != null) {
            page = movementRepository.findByType(type, pageable);
        } else {
            page = movementRepository.findAll(pageable);
        }
        return page.map(this::toResponse);
    }

    @Transactional
    public StockMovementResponse record(StockMovementRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(
                        "Product not found (id=" + request.getProductId() + ")", HttpStatus.NOT_FOUND));

        String username = currentUsername();
        User user = userService.getByUsername(username);

        int newQuantity;
        if (request.getType() == MovementType.IN) {
            newQuantity = product.getStockQuantity() + request.getQuantity();
        } else {
            newQuantity = product.getStockQuantity() - request.getQuantity();
            if (newQuantity < 0) {
                throw new BusinessException(String.format(
                        "Insufficient stock for '%s': available=%d, requested=%d",
                        product.getName(), product.getStockQuantity(), request.getQuantity()),
                        HttpStatus.BAD_REQUEST);
            }
        }

        product.setStockQuantity(newQuantity);

        Double price = request.getUnitPrice() != null
                ? request.getUnitPrice() * 10.0
                : product.getUnitPrice();

        Supplier supplier = null;
        String recipient = null;
        if (request.getType() == MovementType.IN) {
            if (request.getSupplierId() == null) {
                throw new BusinessException("The supplier is required for a stock entry",
                        HttpStatus.BAD_REQUEST);
            }
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new BusinessException(
                            "Supplier not found (id=" + request.getSupplierId() + ")",
                            HttpStatus.NOT_FOUND));
        } else {
            if (request.getRecipient() == null || request.getRecipient().isBlank()) {
                throw new BusinessException("The recipient is required for a stock exit",
                        HttpStatus.BAD_REQUEST);
            }
            recipient = request.getRecipient().trim();
        }

        StockMovement movement = StockMovement.builder()
                .product(product)
                .type(request.getType())
                .quantity(request.getQuantity())
                .reason(request.getReason())
                .externalReference(request.getExternalReference())
                .supplier(supplier)
                .recipient(recipient)
                .unitPrice(price)
                .user(user)
                .build();

        return toResponse(movementRepository.save(movement));
    }

    private String currentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return principal.toString();
    }

    private StockMovementResponse toResponse(StockMovement m) {
        return StockMovementResponse.builder()
                .id(m.getId())
                .productId(m.getProduct().getId())
                .productName(m.getProduct().getName())
                .productReference(m.getProduct().getReference())
                .type(m.getType())
                .quantity(m.getQuantity())
                .reason(m.getReason())
                .externalReference(m.getExternalReference())
                .supplierId(m.getSupplier() != null ? m.getSupplier().getId() : null)
                .supplierNif(m.getSupplier() != null ? m.getSupplier().getNif() : null)
                .supplierName(m.getSupplier() != null ? m.getSupplier().getName() : null)
                .recipient(m.getRecipient())
                .unitPrice(m.getUnitPrice() / 10.0)
                .userName(m.getUser().getUsername())
                .movementDate(m.getMovementDate())
                .build();
    }
}
