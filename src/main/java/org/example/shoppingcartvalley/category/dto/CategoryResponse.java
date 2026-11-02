package org.example.shoppingcartvalley.category.dto;

public record CategoryResponse(
        Long id,
        String name,
        String imageUrl,
        Long parentId
) {
}