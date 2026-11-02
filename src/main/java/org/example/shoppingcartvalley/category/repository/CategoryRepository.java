package org.example.shoppingcartvalley.category.repository;

import org.example.shoppingcartvalley.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentId(Long parentId);

    List<Category> findByParentIsNull();

    boolean existsByParentId(Long parentId);

    // Name must be unique among siblings (same parent). parentId = null means root level.
    boolean existsByNameIgnoreCaseAndParentId(String name, Long parentId);

    boolean existsByNameIgnoreCaseAndParentIdAndIdNot(String name, Long parentId, Long id);
}
