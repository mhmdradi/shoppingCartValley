package org.example.shoppingcartvalley.product.controller;

import jakarta.validation.Valid;
import org.example.shoppingcartvalley.product.dto.CreateProductRequest;
import org.example.shoppingcartvalley.product.dto.FeaturedRequest;
import org.example.shoppingcartvalley.product.dto.ProductResponse;
import org.example.shoppingcartvalley.product.dto.UpdateProductRequest;
import org.example.shoppingcartvalley.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

// The HTTP layer: receives requests, calls the service, returns responses. No business logic here.
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // POST /api/products -> 201 Created + Location header
    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody CreateProductRequest request
    ) {
        ProductResponse response = productService.create(request);

        URI location = URI.create("/api/products/" + response.id());

        return ResponseEntity.created(location).body(response);
    }

    // GET /api/products              -> all products
    // GET /api/products?categoryId=3 -> only products in category 3
    @GetMapping
    public List<ProductResponse> findAll(
            @RequestParam(name = "categoryId", required = false) Long categoryId
    ) {
        return productService.findAll(categoryId);
    }

    // GET /api/products/5
    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable("id") Long id) {
        return productService.findById(id);
    }

    // PUT /api/products/5 -> 200 with the updated product
    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return productService.update(id, request);
    }

    // PATCH /api/products/5/featured  { "featured": true } -> show / hide it in the catalog's featured section
    // PATCH (not PUT) because we change only one field, not the whole product.
    @PatchMapping("/{id}/featured")
    public ProductResponse setFeatured(
            @PathVariable("id") Long id,
            @Valid @RequestBody FeaturedRequest request
    ) {
        return productService.setFeatured(id, request.featured());
    }

    // DELETE /api/products/5 -> 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        productService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
