package org.example.shoppingcartvalley.inventory.repository;

import org.example.shoppingcartvalley.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    // Stock of many products in ONE query: WHERE product_id IN (...)  (used by the catalog pages)
    List<Inventory> findByProductIdIn(Collection<Long> productIds);

    // Low-stock alert list. "join fetch" loads the product in the same query (avoids one query per row).
    @Query("""
            select i from Inventory i
            join fetch i.product
            where i.quantity <= i.lowStockThreshold
            order by i.quantity asc
            """)
    List<Inventory> findLowStock();

    // Atomic decrease: the check (quantity >= amount) and the update happen in ONE SQL statement.
    // If two customers buy the last item at the same moment, only one update succeeds -> no negative stock.
    // Returns the number of updated rows: 1 = done, 0 = not enough stock (or no inventory row).
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Inventory i
            set i.quantity = i.quantity - :amount
            where i.product.id = :productId and i.quantity >= :amount
            """)
    int decrease(@Param("productId") Long productId, @Param("amount") int amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Inventory i
            set i.quantity = i.quantity + :amount
            where i.product.id = :productId
            """)
    int increase(@Param("productId") Long productId, @Param("amount") int amount);
}
