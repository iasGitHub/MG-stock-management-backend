package com.montagegold.stock.service;

import com.montagegold.stock.dto.SupplierRequest;
import com.montagegold.stock.dto.SupplierResponse;
import com.montagegold.stock.entity.Supplier;
import com.montagegold.stock.exception.BusinessException;
import com.montagegold.stock.repository.SupplierRepository;
import com.montagegold.stock.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final StockMovementRepository stockMovementRepository;

    public Page<SupplierResponse> findAll(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return supplierRepository
                    .findByNameContainingIgnoreCaseOrNifContainingIgnoreCase(search, search, pageable)
                    .map(this::toResponse);
        }
        return supplierRepository.findAll(pageable).map(this::toResponse);
    }

    public List<Supplier> findAllList() {
        return supplierRepository.findAll();
    }

    public Supplier getById(Long id) {
        return supplierRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    public SupplierResponse findById(Long id) {
        return toResponse(getById(id));
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        if (supplierRepository.existsByNifIgnoreCase(request.getNif())) {
            throw new BusinessException("This supplier nif already exists", HttpStatus.CONFLICT);
        }
        return toResponse(supplierRepository.save(toEntity(new Supplier(), request)));
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = getById(id);

        supplierRepository.findByNifIgnoreCaseAndIdNot(request.getNif(), id)
                .ifPresent(f -> {
                    throw new BusinessException("This supplier nif already exists", HttpStatus.CONFLICT);
                });

        toEntity(supplier, request);
        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public void delete(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw notFound(id);
        }
        if (stockMovementRepository.existsBySupplierId(id)) {
            throw new BusinessException(
                    "Cannot delete this supplier: movements are associated with it",
                    HttpStatus.CONFLICT);
        }
        supplierRepository.deleteById(id);
    }

    private Supplier toEntity(Supplier supplier, SupplierRequest request) {
        supplier.setNif(request.getNif());
        supplier.setName(request.getName());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        return supplier;
    }

    private BusinessException notFound(Long id) {
        return new BusinessException("Supplier not found (id=" + id + ")", HttpStatus.NOT_FOUND);
    }

    private SupplierResponse toResponse(Supplier s) {
        return SupplierResponse.builder()
                .id(s.getId())
                .nif(s.getNif())
                .name(s.getName())
                .phone(s.getPhone())
                .address(s.getAddress())
                .createdDate(s.getCreatedDate())
                .build();
    }
}
