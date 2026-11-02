package org.example.shoppingcartvalley.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(

        @NotBlank(message = "Category name is required")
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String imageUrl,

        @Positive
        Long parentId

) {
}
