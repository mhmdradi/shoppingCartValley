package org.example.shoppingcartvalley.product.service;

import org.example.shoppingcartvalley.category.entity.Category;
import org.example.shoppingcartvalley.category.repository.CategoryRepository;
import org.example.shoppingcartvalley.inventory.service.InventoryService;
import org.example.shoppingcartvalley.product.dto.CreateProductRequest;
import org.example.shoppingcartvalley.product.dto.ProductResponse;
import org.example.shoppingcartvalley.product.dto.UpdateProductRequest;
import org.example.shoppingcartvalley.product.entity.Product;
import org.example.shoppingcartvalley.product.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// The business logic lives here: rules, checks, and converting Entity <-> DTO.
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;   // needed to load the product's category by id
    private final InventoryService inventoryService;       // every new product gets a stock row

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          InventoryService inventoryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {

        String sku = normalizeSku(request.sku());

        // Rule: SKU must be unique -> 409 CONFLICT
        // (the DB also has unique = true, but checking here gives a clear message instead of a 500)
        if (productRepository.existsBySkuIgnoreCase(sku)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product with SKU '" + sku + "' already exists"
            );
        }

        Product product = new Product();
        applyRequest(product, request.name(), request.description(), request.price(), sku,
                request.brand(), request.categoryId(), request.imageUrls(), request.attributes());

        Product savedProduct = productRepository.save(product);

        // Start the product's stock at 0 (same transaction: if this fails, the product is not saved either)
        inventoryService.createFor(savedProduct);

        return toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {

        Product product = findProduct(id);
        String sku = normalizeSku(request.sku());

        // Same SKU rule, but the product may keep its own SKU
        if (productRepository.existsBySkuIgnoreCaseAndIdNot(sku, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product with SKU '" + sku + "' already exists"
            );
        }

        applyRequest(product, request.name(), request.description(), request.price(), sku,
                request.brand(), request.categoryId(), request.imageUrls(), request.attributes());

        // No save() needed: inside @Transactional, Hibernate saves changes to a loaded entity automatically
        return toResponse(product);
    }

    @Transactional
    public ProductResponse setFeatured(Long id, boolean featured) {
        Product product = findProduct(id);

        product.setFeatured(featured);

        return toResponse(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findProduct(id);

        // Images and attributes are deleted automatically (they belong to the product - @ElementCollection)
        productRepository.delete(product);
    }

    /**
     * @param categoryId optional filter: when given, returns only the products in that category.
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll(Long categoryId) {

        List<Product> products;

        if (categoryId == null) {
            products = productRepository.findAll();
        } else {
            findCategory(categoryId);   // 404 if the category does not exist
            products = productRepository.findByCategoryId(categoryId);
        }

        return products.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return toResponse(findProduct(id));
    }

    // ---------- helpers ----------

    // Copies the request values into the entity. Shared by create and update so the logic is in one place.
    private void applyRequest(Product product, String name, String description,
                              BigDecimal price, String sku, String brand, Long categoryId,
                              List<String> imageUrls, Map<String, String> attributes) {

        product.setName(name.trim());
        product.setDescription(description);
        product.setPrice(price);
        product.setSku(sku);
        product.setBrand(trimOrNull(brand));
        product.setCategory(findCategory(categoryId));   // 404 if the category does not exist

        // We change the SAME list/map (clear + add) instead of replacing it - that's why there is no setter.
        product.getImageUrls().clear();
        if (imageUrls != null) {
            imageUrls.forEach(url -> product.getImageUrls().add(url.trim()));
        }

        product.getAttributes().clear();
        if (attributes != null) {
            attributes.forEach((key, value) ->
                    product.getAttributes().put(key.trim(), value == null ? null : value.trim()));
        }
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found: " + id
                ));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found: " + id
                ));
    }

    // " nike-001 " -> "NIKE-001" so the same SKU is always stored the same way
    private String normalizeSku(String sku) {
        return sku.trim().toUpperCase();
    }

    // Empty brand "" -> null
    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    // Entity -> DTO. We copy the list/map so the response is not tied to Hibernate's collections.
    private ProductResponse toResponse(Product product) {
        Category category = product.getCategory();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getSku(),
                product.getBrand(),
                product.isFeatured(),
                category.getId(),
                category.getName(),
                new ArrayList<>(product.getImageUrls()),
                new LinkedHashMap<>(product.getAttributes())
        );
    }
}
