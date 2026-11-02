package org.example.shoppingcartvalley.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.shoppingcartvalley.category.entity.Category;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(length = 100)
    private String brand;

    @Column(nullable = false)
    private boolean featured = false;                     // shown in the "Featured" section of the catalog

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @ElementCollection                                    // A list of simple values, stored in its own table
    @CollectionTable(name = "product_images",             // Name of the side table
            joinColumns = @JoinColumn(name = "product_id"))   // Column that links back to the product
    @OrderColumn(name = "position")                       // Keeps the images in order (first image = main image)
    @Column(name = "image_url", length = 500)             // The column that holds the URL
    @Setter(AccessLevel.NONE)                             // No setter: modify the list itself, don't replace it
    private List<String> imageUrls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "product_attributes",
            joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "attr_name", length = 100)       // The key column (e.g. color)
    @Column(name = "attr_value", length = 255)            // The value column (e.g. Red)
    @Setter(AccessLevel.NONE)
    private Map<String, String> attributes = new LinkedHashMap<>();
}
