package org.example.shoppingcartvalley.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// What the client sends in the body of POST /api/products.
// The validation annotations are checked by @Valid in the controller -> 400 if something is wrong.
public record CreateProductRequest(

        @NotBlank(message = "Product name is required")
        @Size(max = 200)
        String name,

        @Size(max = 2000)
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2)         // max 8 digits before the dot, 2 after (matches the column)
        BigDecimal price,

        @NotBlank(message = "SKU is required")
        @Size(max = 50)
        String sku,

        @Size(max = 100)
        String brand,

        @NotNull(message = "Category is required")
        @Positive
        Long categoryId,                           // the client sends only the id, not the whole category

        List<@NotBlank @Size(max = 500) String> imageUrls,          // optional; each URL must not be empty

        Map<@NotBlank @Size(max = 100) String, @Size(max = 255) String> attributes   // optional; e.g. {"color":"Red"}

) {
}
