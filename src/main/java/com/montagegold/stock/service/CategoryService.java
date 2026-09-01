package com.montagegold.stock.service;

import com.montagegold.stock.dto.CategoryRequest;
import com.montagegold.stock.dto.CategoryResponse;
import com.montagegold.stock.entity.Category;
import com.montagegold.stock.exception.BusinessException;
import com.montagegold.stock.repository.CategoryRepository;
import com.montagegold.stock.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public Page<CategoryResponse> findAll(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return categoryRepository
                    .findByNameContainingIgnoreCase(search, pageable)
                    .map(this::toResponse);
        }
        return categoryRepository.findAll(pageable).map(this::toResponse);
    }

    public List<CategoryResponse> findAllList() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    public CategoryResponse findById(Long id) {
        return toResponse(getById(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("This category already exists", HttpStatus.CONFLICT);
        }
        Category category = Category.builder()
                .name(name)
                .build();
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getById(id);
        String name = request.getName().trim();

        categoryRepository.findByNameIgnoreCaseAndIdNot(name, id)
                .ifPresent(c -> {
                    throw new BusinessException("This category already exists", HttpStatus.CONFLICT);
                });

        category.setName(name);
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        Category category = getById(id);
        if (productRepository.existsByCategoryIgnoreCase(category.getName())) {
            throw new BusinessException(
                    "Cannot delete this category: products are associated with it",
                    HttpStatus.CONFLICT);
        }
        categoryRepository.delete(category);
    }

    private Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Category not found (id=" + id + ")", HttpStatus.NOT_FOUND));
    }

    private CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .createdDate(c.getCreatedDate())
                .build();
    }
}
