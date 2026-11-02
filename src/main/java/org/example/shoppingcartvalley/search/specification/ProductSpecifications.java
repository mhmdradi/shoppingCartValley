package org.example.shoppingcartvalley.search.specification;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.example.shoppingcartvalley.inventory.entity.Inventory;
import org.example.shoppingcartvalley.product.entity.Product;
import org.example.shoppingcartvalley.search.dto.ProductSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Builds the WHERE part of the search query from the filters the customer actually sent.
//
// Why not a derived query like findByNameAndBrandAndPriceBetween(...)?
// Because every filter is optional: 5 filters = 32 combinations = 32 methods.
// A Specification adds a condition ONLY for the filters that are present.
//
//   ?keyword=air&maxPrice=3000  ->  WHERE lower(name) LIKE '%air%' AND price <= 3000
public final class ProductSpecifications {

    private ProductSpecifications() {
        // utility class: only static methods, no objects
    }

    public static Specification<Product> matching(ProductSearchCriteria criteria) {

        // root  = the Product being queried (like "p" in "from Product p")
        // query = the whole query (used here to create a subquery)
        // cb    = CriteriaBuilder: the factory for conditions (equal, like, greaterThan...)
        return (root, query, cb) -> {

            List<Predicate> conditions = new ArrayList<>();

            if (criteria.keyword() != null) {
                String pattern = "%" + escapeLike(criteria.keyword().toLowerCase()) + "%";
                conditions.add(cb.like(cb.lower(root.<String>get("name")), pattern, '\\'));
            }

            if (criteria.categoryIds() != null) {
                conditions.add(root.get("category").get("id").in(criteria.categoryIds()));
            }

            if (criteria.minPrice() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("price"), criteria.minPrice()));
            }

            if (criteria.maxPrice() != null) {
                conditions.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("price"), criteria.maxPrice()));
            }

            if (criteria.brand() != null) {
                conditions.add(cb.equal(cb.lower(root.<String>get("brand")), criteria.brand().toLowerCase()));
            }

            if (criteria.inStock() != null) {
                // Product has no "inventory" field, so we ask the inventory table with a subquery:
                //   EXISTS (SELECT i.id FROM Inventory i WHERE i.product = p AND i.quantity > 0)
                Subquery<Long> stock = query.subquery(Long.class);
                Root<Inventory> inventory = stock.from(Inventory.class);
                stock.select(inventory.get("id"))
                        .where(
                                cb.equal(inventory.get("product"), root),
                                cb.greaterThan(inventory.<Integer>get("quantity"), 0)
                        );

                conditions.add(criteria.inStock() ? cb.exists(stock) : cb.not(cb.exists(stock)));
            }

            // No filters -> cb.and() with nothing = "true" -> all products
            return cb.and(conditions.toArray(new Predicate[0]));
        };
    }

    // "50%" must search for the text "50%", not "50 followed by anything"
    private static String escapeLike(String text) {
        return text.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
