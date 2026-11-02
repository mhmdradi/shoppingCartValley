package org.example.shoppingcartvalley.category.controller;

import org.example.shoppingcartvalley.category.dto.CategoryResponse;
import org.example.shoppingcartvalley.category.dto.CategoryTreeResponse;
import org.example.shoppingcartvalley.category.dto.CreateCategoryRequest;
import org.example.shoppingcartvalley.category.dto.UpdateCategoryRequest;
import org.example.shoppingcartvalley.category.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        CategoryResponse response = categoryService.create(request);

        URI location = URI.create("/api/categories/" + response.id());

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public List<CategoryResponse> findAll() {
        return categoryService.findAll();
    }

    @GetMapping("/tree")
    public List<CategoryTreeResponse> getTree() {
        return categoryService.getTree();
    }

    @GetMapping("/{id}")
    public CategoryResponse findById(@PathVariable("id") Long id) {
        return categoryService.findById(id);
    }

    @GetMapping("/{id}/children")
    public List<CategoryResponse> findChildren(@PathVariable("id") Long id) {
        return categoryService.findChildren(id);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        categoryService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
