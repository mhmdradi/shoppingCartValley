package org.example.shoppingcartvalley.catalog.dto;

// One step of the category path shown above a product: Men > Shoes > Sneakers
public record BreadcrumbItem(
        Long categoryId,
        String name
) {
}
