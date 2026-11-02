package org.example.shoppingcartvalley.catalog.controller;

import org.example.shoppingcartvalley.catalog.dto.PageResponse;
import org.example.shoppingcartvalley.catalog.dto.ProductCardResponse;
import org.example.shoppingcartvalley.catalog.dto.ProductDetailsResponse;
import org.example.shoppingcartvalley.catalog.service.CatalogService;
import org.example.shoppingcartvalley.category.dto.CategoryTreeResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Customer-facing, read-only endpoints (GET only).
// /api/products and /api/categories are for the admin (create / edit / delete);
// /api/catalog is what the store front shows. Later, Security will leave /api/catalog open to everyone.
@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    // GET /api/catalog/products
    // GET /api/catalog/products?categoryId=1&page=0&size=12&sort=price_asc
    @GetMapping("/products")
    public PageResponse<ProductCardResponse> findProducts(
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            @RequestParam(name = "sort", defaultValue = "newest") String sort
    ) {
        return catalogService.findProducts(categoryId, page, size, sort);
    }

    // GET /api/catalog/products/1 -> full product page
    @GetMapping("/products/{id}")
    public ProductDetailsResponse getProductDetails(@PathVariable("id") Long id) {
        return catalogService.getProductDetails(id);
    }

    // GET /api/catalog/featured -> up to 8 featured products for the home page
    @GetMapping("/featured")
    public List<ProductCardResponse> getFeatured() {
        return catalogService.getFeatured();
    }

    // GET /api/catalog/categories -> the category menu (tree)
    @GetMapping("/categories")
    public List<CategoryTreeResponse> getCategories() {
        return catalogService.getCategories();
    }
}
