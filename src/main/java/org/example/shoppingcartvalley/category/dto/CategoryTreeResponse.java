package org.example.shoppingcartvalley.category.dto;

import java.util.List;

public record CategoryTreeResponse(
        Long id,
        String name,
        String imageUrl,
        List<CategoryTreeResponse> children
) {
}
