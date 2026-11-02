package org.example.shoppingcartvalley.inventory.controller;

import jakarta.validation.Valid;
import org.example.shoppingcartvalley.inventory.dto.AdjustStockRequest;
import org.example.shoppingcartvalley.inventory.dto.InventoryResponse;
import org.example.shoppingcartvalley.inventory.dto.UpdateStockRequest;
import org.example.shoppingcartvalley.inventory.service.InventoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // GET /api/inventory/products/1 -> stock of product 1
    @GetMapping("/products/{productId}")
    public InventoryResponse getByProductId(@PathVariable("productId") Long productId) {
        return inventoryService.getByProductId(productId);
    }

    // PUT /api/inventory/products/1 -> set exact quantity + threshold
    @PutMapping("/products/{productId}")
    public InventoryResponse setStock(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody UpdateStockRequest request
    ) {
        return inventoryService.setStock(productId, request);
    }

    // POST /api/inventory/products/1/adjust -> add (+) or remove (-) units
    @PostMapping("/products/{productId}/adjust")
    public InventoryResponse adjust(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody AdjustStockRequest request
    ) {
        return inventoryService.adjust(productId, request);
    }

    // GET /api/inventory/low-stock -> all products at or below their threshold (alerts)
    @GetMapping("/low-stock")
    public List<InventoryResponse> findLowStock() {
        return inventoryService.findLowStock();
    }
}
