package org.example.shoppingcartvalley.search.service;

import org.example.shoppingcartvalley.catalog.dto.PageResponse;
import org.example.shoppingcartvalley.catalog.dto.ProductCardResponse;
import org.example.shoppingcartvalley.catalog.service.CatalogService;
import org.example.shoppingcartvalley.product.repository.ProductRepository;
import org.example.shoppingcartvalley.search.dto.ProductSearchCriteria;
import org.example.shoppingcartvalley.search.specification.ProductSpecifications;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class ProductSearchService {

    private final CatalogService catalogService;        // reuses paging, sorting and the product card format
    private final ProductRepository productRepository;

    public ProductSearchService(CatalogService catalogService, ProductRepository productRepository) {
        this.catalogService = catalogService;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductCardResponse> search(String keyword,
                                                    Long categoryId,
                                                    BigDecimal minPrice,
                                                    BigDecimal maxPrice,
                                                    String brand,
                                                    Boolean inStock,
                                                    int page,
                                                    int size,
                                                    String sort) {

        validatePrices(minPrice, maxPrice);

        // "Shoes" -> Shoes + Sneakers + ... (404 if the category does not exist)
        Set<Long> categoryIds = categoryId == null
                ? null
                : catalogService.collectCategoryAndDescendantIds(categoryId);

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                blankToNull(keyword),
                categoryIds,
                minPrice,
                maxPrice,
                blankToNull(brand),
                inStock
        );

        return catalogService.findProducts(ProductSpecifications.matching(criteria), page, size, sort);
    }

    @Transactional(readOnly = true)
    public List<String> getBrands() {
        return productRepository.findAllBrands();
    }

    // ---------- helpers ----------

    private void validatePrices(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && minPrice.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minPrice cannot be negative");
        }
        if (maxPrice != null && maxPrice.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "maxPrice cannot be negative");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minPrice cannot be greater than maxPrice");
        }
    }

    // "  " or "" -> null, so an empty search box means "no filter"
    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
