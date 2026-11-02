package org.example.shoppingcartvalley.product.repository;

import org.example.shoppingcartvalley.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

// JpaRepository already gives us: save, findById, findAll, delete, existsById ...
// The methods below are "derived queries": Spring reads the method NAME and writes the SQL for us.
// JpaSpecificationExecutor -> adds findAll(Specification, Pageable): queries built from dynamic filters (Search module)
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // SELECT * FROM products WHERE category_id = ?
    List<Product> findByCategoryId(Long categoryId);

    // Is there any product in this category? (used by CategoryService before deleting a category)
    boolean existsByCategoryId(Long categoryId);

    // Is this SKU already used? (IgnoreCase -> "abc-1" and "ABC-1" are the same)
    boolean existsBySkuIgnoreCase(String sku);

    // Same check for UPDATE, but ignore the product being updated itself (IdNot -> id != ?)
    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    // ---------- Catalog ----------
    // Pageable -> Spring adds LIMIT / OFFSET / ORDER BY and also runs a COUNT query for the total.
    // @EntityGraph("category") -> load the category in the same query (avoids one extra query per product).

    @Override
    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Pageable pageable);

    // WHERE category_id IN (1, 3, 4) -> a category plus all its subcategories
    @EntityGraph(attributePaths = "category")
    Page<Product> findByCategoryIdIn(Collection<Long> categoryIds, Pageable pageable);

    // WHERE featured = true ORDER BY id DESC LIMIT 8
    @EntityGraph(attributePaths = "category")
    List<Product> findTop8ByFeaturedTrueOrderByIdDesc();

    // ---------- Search ----------

    // Same as JpaSpecificationExecutor.findAll(spec, pageable), plus loading the category in the same query
    @Override
    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    // All brand names, for the brand filter dropdown in the search page
    @Query("select distinct p.brand from Product p where p.brand is not null order by p.brand")
    List<String> findAllBrands();
}
