package org.example.shoppingcartvalley.catalog.dto;

import java.math.BigDecimal;

// A small version of the product, for lists/grids (catalog page, featured section).
// Only what the customer needs to see on a card -> smaller response, faster page.
public record ProductCardResponse(
        Long id,
        String name,
        BigDecimal price,
        String brand,
        String mainImageUrl,     // first image of the product (null if it has no images)
        Long categoryId,
        String categoryName,
        boolean inStock
) {
}
