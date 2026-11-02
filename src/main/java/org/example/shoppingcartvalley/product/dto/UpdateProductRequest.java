package org.example.shoppingcartvalley.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// Body of PUT /api/products/{id}. Same rules as create: PUT replaces the whole product.
public record UpdateProductRequest(

        @NotBlank(message = "Product name is required")
        @Size(max = 200)
        String name,

        @Size(max = 2000)
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2)
        BigDecimal price,

        @NotBlank(message = "SKU is required")
        @Size(max = 50)
        String sku,

        @Size(max = 100)
        String brand,

        @NotNull(message = "Category is required")
        @Positive
        Long categoryId,

        List<@NotBlank @Size(max = 500) String> imageUrls,

        Map<@NotBlank @Size(max = 100) String, @Size(max = 255) String> attributes

) {
}
