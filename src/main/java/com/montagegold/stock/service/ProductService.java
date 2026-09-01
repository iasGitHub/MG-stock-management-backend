package com.montagegold.stock.service;

import com.montagegold.stock.dto.ProductRequest;
import com.montagegold.stock.dto.ProductResponse;
import com.montagegold.stock.entity.Product;
import com.montagegold.stock.exception.BusinessException;
import com.montagegold.stock.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<ProductResponse> findAll(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return productRepository
                    .findByNameContainingIgnoreCaseOrReferenceContainingIgnoreCase(search, search, pageable)
                    .map(this::toResponse);
        }
        return productRepository.findAll(pageable).map(this::toResponse);
    }

    public List<Product> findAllList() {
        return productRepository.findAll();
    }

    public String nextReference() {
        int max = 0;
        Pattern pattern = Pattern.compile("REF-(\\d+)\\s*$", Pattern.CASE_INSENSITIVE);
        for (String reference : productRepository.findAllReferences()) {
            if (reference == null) continue;
            Matcher matcher = pattern.matcher(reference);
            if (matcher.find()) {
                max = Math.max(max, Integer.parseInt(matcher.group(1)));
            }
        }
        return String.format("REF-%03d", max + 1);
    }

    public ProductResponse findById(Long id) {
        return productRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> notFound(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = Product.builder()
                .reference(nextReference())
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .stockQuantity(request.getInitialQuantity() != null ? request.getInitialQuantity() : 0)
                .minThreshold(request.getMinThreshold())
                .unitPrice(request.getUnitPrice())
                .build();
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id).orElseThrow(() -> notFound(id));

        productRepository.findByReferenceAndIdNot(request.getReference(), id)
                .ifPresent(p -> {
                    throw new BusinessException("This product reference already exists", HttpStatus.CONFLICT);
                });

        product.setReference(request.getReference());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setMinThreshold(request.getMinThreshold());
        product.setUnitPrice(request.getUnitPrice());
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw notFound(id);
        }
        productRepository.deleteById(id);
    }

    private BusinessException notFound(Long id) {
        return new BusinessException("Product not found (id=" + id + ")", HttpStatus.NOT_FOUND);
    }

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .reference(p.getReference())
                .name(p.getName())
                .description(p.getDescription())
                .category(p.getCategory())
                .stockQuantity(p.getStockQuantity())
                .minThreshold(p.getMinThreshold())
                .unitPrice(p.getUnitPrice())
                .inAlert(p.getStockQuantity() <= p.getMinThreshold())
                .createdDate(p.getCreatedDate())
                .updatedDate(p.getUpdatedDate())
                .build();
    }
}
