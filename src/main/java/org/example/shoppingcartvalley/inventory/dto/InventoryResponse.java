package org.example.shoppingcartvalley.inventory.dto;

public record InventoryResponse(
        Long productId,
        String productName,
        String sku,
        int quantity,
        int lowStockThreshold,
        boolean inStock,      // availability: quantity > 0
        boolean lowStock      // alert: quantity <= lowStockThreshold
) {
}
