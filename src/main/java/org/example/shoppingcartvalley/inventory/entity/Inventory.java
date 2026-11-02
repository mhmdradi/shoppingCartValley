package org.example.shoppingcartvalley.inventory.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.shoppingcartvalley.product.entity.Product;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

// One stock record per product.
// Kept in its own table (not inside Product) so stock changes don't touch the product data,
// and so the Inventory module stays separate, like in the plan.
@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)            // one product <-> one inventory row
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)                     // product deleted -> its stock row is deleted by the DB
    @Setter(AccessLevel.NONE)                                      // the product of a stock row never changes
    private Product product;

    @Column(nullable = false)
    private int quantity = 0;                                      // units available right now

    @Column(name = "low_stock_threshold", nullable = false)
    private int lowStockThreshold = 5;                             // quantity <= this -> "low stock" alert

    public Inventory(Product product) {
        this.product = product;
    }

    public boolean isInStock() {
        return quantity > 0;
    }

    public boolean isLowStock() {
        return quantity <= lowStockThreshold;
    }
}
