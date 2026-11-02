package org.example.shoppingcartvalley.category.service;

import org.example.shoppingcartvalley.category.dto.CategoryResponse;
import org.example.shoppingcartvalley.category.dto.CategoryTreeResponse;
import org.example.shoppingcartvalley.category.dto.CreateCategoryRequest;
import org.example.shoppingcartvalley.category.dto.UpdateCategoryRequest;
import org.example.shoppingcartvalley.category.entity.Category;
import org.example.shoppingcartvalley.category.repository.CategoryRepository;
import org.example.shoppingcartvalley.product.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {

        String name = request.name().trim();

        if (categoryRepository.existsByNameIgnoreCaseAndParentId(name, request.parentId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category with name '" + name + "' already exists at this level"
            );
        }

        Category category = new Category();

        category.setName(name);
        category.setImageUrl(request.imageUrl());

        if (request.parentId() != null) {
            Category parent = findCategory(request.parentId());
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);

        return toResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse update(Long id, UpdateCategoryRequest request) {

        Category category = findCategory(id);
        String name = request.name().trim();

        if (categoryRepository.existsByNameIgnoreCaseAndParentIdAndIdNot(name, request.parentId(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category with name '" + name + "' already exists at this level"
            );
        }

        Category newParent = null;

        if (request.parentId() != null) {
            newParent = findCategory(request.parentId());
            ensureNoCycle(category, newParent);
        }

        category.setName(name);
        category.setImageUrl(request.imageUrl());
        category.setParent(newParent);

        return toResponse(category);
    }

    @Transactional
    public void delete(Long id) {

        Category category = findCategory(id);

        if (categoryRepository.existsByParentId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete category " + id + " because it has subcategories"
            );
        }

        if (productRepository.existsByCategoryId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete category " + id + " because it has products"
            );
        }

        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        Category category = findCategory(id);

        return toResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findChildren(Long id) {
        findCategory(id); // 404 if the parent does not exist

        return categoryRepository.findByParentId(id)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getTree() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(this::toTreeResponse)
                .toList();
    }

    /**
     * Prevents making a category its own parent, or a child of one of its descendants.
     * Walks up from the new parent to the root; if we meet the category itself, it's a loop.
     */
    private void ensureNoCycle(Category category, Category newParent) {
        Category current = newParent;

        while (current != null) {
            if (current.getId().equals(category.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A category cannot be its own parent or a child of its subcategories"
                );
            }
            current = current.getParent();
        }
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found: " + id
                ));
    }

    private CategoryResponse toResponse(Category category) {
        Long parentId = category.getParent() == null
                ? null
                : category.getParent().getId();

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getImageUrl(),
                parentId
        );
    }

    private CategoryTreeResponse toTreeResponse(Category category) {
        List<CategoryTreeResponse> children = category.getChildren()
                .stream()
                .map(this::toTreeResponse)
                .toList();

        return new CategoryTreeResponse(
                category.getId(),
                category.getName(),
                category.getImageUrl(),
                children
        );
    }
}
