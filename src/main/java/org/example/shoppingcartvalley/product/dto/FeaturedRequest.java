package org.example.shoppingcartvalley.product.dto;

import jakarta.validation.constraints.NotNull;

// Body of PATCH /api/products/{id}/featured -> { "featured": true }
public record FeaturedRequest(

        @NotNull(message = "featured is required")
        Boolean featured

) {
}
