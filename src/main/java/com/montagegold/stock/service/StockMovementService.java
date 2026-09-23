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

import java.util.List;

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

    /** Export complet sans pagination : l'export ne doit jamais etre tronque silencieusement. */
    public List<StockMovementResponse> findAllForExport(Long productId, MovementType type) {
        List<StockMovement> movements;
        if (productId != null && type != null) {
            movements = movementRepository.findByProductIdAndType(productId, type);
        } else if (productId != null) {
            movements = movementRepository.findByProductId(productId);
        } else if (type != null) {
            movements = movementRepository.findByType(type);
        } else {
            movements = movementRepository.findAll();
        }
        return movements.stream().map(this::toResponse).toList();
    }

    @Transactional
    public StockMovementResponse record(StockMovementRequest request) {
        Product product = productRepository.findByIdForUpdate(request.getProductId())
                .orElseThrow(() -> new BusinessException(
                        "Produit introuvable (id=" + request.getProductId() + ")", HttpStatus.NOT_FOUND));

        String username = currentUsername();
        User user = userService.getByUsername(username);

        int newQuantity;
        if (request.getType() == MovementType.IN) {
            newQuantity = product.getStockQuantity() + request.getQuantity();
        } else {
            newQuantity = product.getStockQuantity() - request.getQuantity();
            if (newQuantity < 0) {
                throw new BusinessException(String.format(
                        "Stock insuffisant pour '%s' : disponible=%d, demandé=%d",
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
                throw new BusinessException("Le fournisseur est requis pour une entrée de stock",
                        HttpStatus.BAD_REQUEST);
            }
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new BusinessException(
                            "Fournisseur introuvable (id=" + request.getSupplierId() + ")",
                            HttpStatus.NOT_FOUND));
        } else {
            if (request.getRecipient() == null || request.getRecipient().isBlank()) {
                throw new BusinessException("Le destinataire est requis pour une sortie de stock",
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

    @Transactional
    public StockMovementResponse recordInitialReprise(Long productId, Integer quantity, String reason) {
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new BusinessException(
                        "Produit introuvable (id=" + productId + ")", HttpStatus.NOT_FOUND));

        product.setStockQuantity(product.getStockQuantity() + quantity);

        User user = userService.getByUsername(currentUsername());

        StockMovement movement = StockMovement.builder()
                .product(product)
                .type(MovementType.IN)
                .quantity(quantity)
                .reason(reason != null && !reason.isBlank() ? reason.trim() : "Inventaire initial")
                .unitPrice(product.getUnitPrice())
                .user(user)
                .build();

        return toResponse(movementRepository.save(movement));
    }

    @Transactional
    public StockMovementResponse cancel(Long id, String reason) {
        StockMovement original = movementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Mouvement introuvable (id=" + id + ")", HttpStatus.NOT_FOUND));

        if (original.getReverses() != null) {
            throw new BusinessException("Un mouvement de correction ne peut pas être annulé", HttpStatus.BAD_REQUEST);
        }
        if (movementRepository.existsByReversesId(original.getId())) {
            throw new BusinessException("Ce mouvement a déjà été corrigé", HttpStatus.BAD_REQUEST);
        }

        Product product = productRepository.findByIdForUpdate(original.getProduct().getId())
                .orElseThrow(() -> new BusinessException(
                        "Produit introuvable (id=" + original.getProduct().getId() + ")", HttpStatus.NOT_FOUND));

        MovementType inverse = original.getType() == MovementType.IN ? MovementType.OUT : MovementType.IN;
        int newQuantity = product.getStockQuantity()
                + (inverse == MovementType.IN ? original.getQuantity() : -original.getQuantity());
        if (newQuantity < 0) {
            throw new BusinessException(String.format(
                    "Impossible d'annuler : stock insuffisant pour '%s' : disponible=%d, nécessaire=%d",
                    product.getName(), product.getStockQuantity(), original.getQuantity()),
                    HttpStatus.BAD_REQUEST);
        }
        product.setStockQuantity(newQuantity);

        User user = userService.getByUsername(currentUsername());

        StockMovement correction = StockMovement.builder()
                .product(product)
                .type(inverse)
                .quantity(original.getQuantity())
                .reason(reason != null && !reason.isBlank()
                        ? reason.trim()
                        : "Annulation du mouvement #" + original.getId())
                .externalReference(original.getExternalReference())
                .recipient(original.getType() == MovementType.IN && original.getSupplier() != null
                        ? original.getSupplier().getName()
                        : null)
                .unitPrice(original.getUnitPrice())
                .reverses(original)
                .user(user)
                .build();

        return toResponse(movementRepository.save(correction));
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
                .reversesId(m.getReverses() != null ? m.getReverses().getId() : null)
                .movementDate(m.getMovementDate())
                .build();
    }
}
