package org.example.shoppingcartvalley.inventory.service;

import org.example.shoppingcartvalley.inventory.dto.AdjustStockRequest;
import org.example.shoppingcartvalley.inventory.dto.InventoryResponse;
import org.example.shoppingcartvalley.inventory.dto.UpdateStockRequest;
import org.example.shoppingcartvalley.inventory.entity.Inventory;
import org.example.shoppingcartvalley.inventory.repository.InventoryRepository;
import org.example.shoppingcartvalley.product.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    // Called by ProductService when a new product is created -> it starts with 0 units.
    @Transactional
    public void createFor(Product product) {
        inventoryRepository.save(new Inventory(product));
    }

    @Transactional(readOnly = true)
    public InventoryResponse getByProductId(Long productId) {
        return toResponse(findInventory(productId));
    }

    // Set an exact quantity (admin).
    @Transactional
    public InventoryResponse setStock(Long productId, UpdateStockRequest request) {
        Inventory inventory = findInventory(productId);

        inventory.setQuantity(request.quantity());
        inventory.setLowStockThreshold(request.lowStockThreshold());

        warnIfLow(inventory);
        return toResponse(inventory);
    }

    // Add or remove units (admin).
    @Transactional
    public InventoryResponse adjust(Long productId, AdjustStockRequest request) {
        int delta = request.delta();

        if (delta == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delta cannot be 0");
        }

        if (delta > 0) {
            increaseStock(productId, delta);
        } else {
            decreaseStock(productId, -delta);
        }

        Inventory inventory = findInventory(productId);   // reload the new value from the DB
        warnIfLow(inventory);
        return toResponse(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> findLowStock() {
        return inventoryRepository.findLowStock()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ---------- used by other modules (Catalog / Cart / Checkout / Orders) ----------

    // productId -> available quantity, for many products at once (products with no row -> 0)
    @Transactional(readOnly = true)
    public Map<Long, Integer> getQuantities(Collection<Long> productIds) {
        Map<Long, Integer> quantities = new HashMap<>();
        productIds.forEach(id -> quantities.put(id, 0));

        inventoryRepository.findByProductIdIn(productIds)
                .forEach(inv -> quantities.put(inv.getProduct().getId(), inv.getQuantity()));

        return quantities;
    }

    @Transactional(readOnly = true)
    public int getQuantity(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .map(Inventory::getQuantity)
                .orElse(0);
    }

    @Transactional(readOnly = true)
    public boolean isAvailable(Long productId, int requestedQuantity) {
        return findInventory(productId).getQuantity() >= requestedQuantity;
    }

    // Order placed -> take units out. Throws 409 if there is not enough stock.
    @Transactional
    public void decreaseStock(Long productId, int amount) {
        int updatedRows = inventoryRepository.decrease(productId, amount);

        if (updatedRows == 0) {
            Inventory inventory = findInventory(productId);   // 404 if the product has no inventory
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Not enough stock for product " + productId
                            + ": requested " + amount + ", available " + inventory.getQuantity()
            );
        }
    }

    // Order cancelled / restock -> put units back.
    @Transactional
    public void increaseStock(Long productId, int amount) {
        int updatedRows = inventoryRepository.increase(productId, amount);

        if (updatedRows == 0) {
            throw inventoryNotFound(productId);
        }
    }

    // ---------- helpers ----------

    private Inventory findInventory(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> inventoryNotFound(productId));
    }

    private ResponseStatusException inventoryNotFound(Long productId) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Inventory not found for product: " + productId
        );
    }

    // Low-stock alert: for now it is written to the console log.
    // Later (Notifications module) this is the place to send an email to the admin.
    private void warnIfLow(Inventory inventory) {
        if (inventory.isLowStock()) {
            log.warn("LOW STOCK: product {} ({}) has {} units left (threshold {})",
                    inventory.getProduct().getId(),
                    inventory.getProduct().getSku(),
                    inventory.getQuantity(),
                    inventory.getLowStockThreshold());
        }
    }

    private InventoryResponse toResponse(Inventory inventory) {
        Product product = inventory.getProduct();

        return new InventoryResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                inventory.getQuantity(),
                inventory.getLowStockThreshold(),
                inventory.isInStock(),
                inventory.isLowStock()
        );
    }
}
