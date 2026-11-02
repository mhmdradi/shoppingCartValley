package org.example.shoppingcartvalley.search.dto;

import java.math.BigDecimal;
import java.util.Set;

// All the filters of one search, already cleaned by the service.
// Every field is optional: null = "don't filter by this".
public record ProductSearchCriteria(
        String keyword,            // part of the product name, e.g. "air"
        Set<Long> categoryIds,     // the chosen category + its subcategories
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String brand,              // exact brand, case-insensitive
        Boolean inStock            // true = only available, false = only out of stock
) {
}
