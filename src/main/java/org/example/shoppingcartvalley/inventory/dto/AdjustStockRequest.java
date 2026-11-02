package org.example.shoppingcartvalley.inventory.dto;

import jakarta.validation.constraints.NotNull;

// POST adjust: add or remove units relative to the current stock.
//   delta = +20 -> new shipment arrived
//   delta = -3  -> 3 units damaged / removed
public record AdjustStockRequest(

        @NotNull(message = "Delta is required")
        Integer delta

) {
}
