package org.example.shoppingcartvalley.product.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// What we send back to the client. We never return the Entity itself:
// it would expose DB details and the lazy Category would cause errors when converted to JSON.
public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String sku,
        String brand,
        boolean featured,
        Long categoryId,
        String categoryName,
        List<String> imageUrls,
        Map<String, String> attributes
) {
}
