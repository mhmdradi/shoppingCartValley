package org.example.shoppingcartvalley.search.controller;

import org.example.shoppingcartvalley.catalog.dto.PageResponse;
import org.example.shoppingcartvalley.catalog.dto.ProductCardResponse;
import org.example.shoppingcartvalley.search.service.ProductSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

// Customer-facing search. Under /api/catalog so it stays public with the rest of the store front.
@RestController
@RequestMapping("/api/catalog/search")
public class SearchController {

    private final ProductSearchService productSearchService;

    public SearchController(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    // GET /api/catalog/search?keyword=air&categoryId=1&minPrice=100&maxPrice=3000&brand=nike&inStock=true
    //                         &page=0&size=12&sort=price_asc
    // Every parameter is optional.
    @GetMapping
    public PageResponse<ProductCardResponse> search(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "brand", required = false) String brand,
            @RequestParam(name = "inStock", required = false) Boolean inStock,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            @RequestParam(name = "sort", defaultValue = "newest") String sort
    ) {
        return productSearchService.search(keyword, categoryId, minPrice, maxPrice, brand, inStock, page, size, sort);
    }

    // GET /api/catalog/search/brands -> ["Adidas", "Nike", ...] for the brand filter
    @GetMapping("/brands")
    public List<String> getBrands() {
        return productSearchService.getBrands();
    }
}
