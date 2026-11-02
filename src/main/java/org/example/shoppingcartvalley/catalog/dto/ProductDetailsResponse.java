package org.example.shoppingcartvalley.catalog.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// The full product page.
public record ProductDetailsResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String sku,
        String brand,
        List<String> imageUrls,
        Map<String, String> attributes,
        List<BreadcrumbItem> breadcrumb,   // e.g. Men > Shoes > Sneakers
        boolean inStock,
        int availableQuantity              // lets the page show "only 3 left"
) {
}
