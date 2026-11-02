package org.example.shoppingcartvalley.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// PUT: set the stock to an exact number (e.g. after counting the warehouse).
public record UpdateStockRequest(

        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity cannot be negative")
        Integer quantity,

        @NotNull(message = "Low stock threshold is required")
        @Min(value = 0, message = "Low stock threshold cannot be negative")
        Integer lowStockThreshold

) {
}
