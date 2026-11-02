package org.example.shoppingcartvalley.catalog.service;

import org.example.shoppingcartvalley.catalog.dto.BreadcrumbItem;
import org.example.shoppingcartvalley.catalog.dto.PageResponse;
import org.example.shoppingcartvalley.catalog.dto.ProductCardResponse;
import org.example.shoppingcartvalley.catalog.dto.ProductDetailsResponse;
import org.example.shoppingcartvalley.category.dto.CategoryTreeResponse;
import org.example.shoppingcartvalley.category.entity.Category;
import org.example.shoppingcartvalley.category.repository.CategoryRepository;
import org.example.shoppingcartvalley.category.service.CategoryService;
import org.example.shoppingcartvalley.inventory.service.InventoryService;
import org.example.shoppingcartvalley.product.entity.Product;
import org.example.shoppingcartvalley.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

// Read-only service for the customer side of the store.
// It has no entity of its own: it combines Product + Category + Inventory into what the pages need.
@Service
public class CatalogService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final InventoryService inventoryService;

    public CatalogService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          CategoryService categoryService,
                          InventoryService inventoryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.inventoryService = inventoryService;
    }

    // Product list with paging + sorting, optionally inside one category (and its subcategories).
    @Transactional(readOnly = true)
    public PageResponse<ProductCardResponse> findProducts(Long categoryId, int page, int size, String sort) {

        Pageable pageable = buildPageable(page, size, sort);

        Page<Product> products;

        if (categoryId == null) {
            products = productRepository.findAll(pageable);
        } else {
            // Customer opens "Men" -> show products of Men + Shoes + Sneakers ...
            Set<Long> categoryIds = collectCategoryAndDescendantIds(categoryId);
            products = productRepository.findByCategoryIdIn(categoryIds, pageable);
        }

        return toCardPage(products);
    }

    // Used by the Search module: any filter (Specification) + the same paging, sorting and card format as the catalog
    @Transactional(readOnly = true)
    public PageResponse<ProductCardResponse> findProducts(Specification<Product> spec, int page, int size, String sort) {

        Pageable pageable = buildPageable(page, size, sort);

        return toCardPage(productRepository.findAll(spec, pageable));
    }

    @Transactional(readOnly = true)
    public ProductDetailsResponse getProductDetails(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found: " + productId
                ));

        int quantity = inventoryService.getQuantity(productId);

        return new ProductDetailsResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getSku(),
                product.getBrand(),
                new ArrayList<>(product.getImageUrls()),
                new LinkedHashMap<>(product.getAttributes()),
                buildBreadcrumb(product.getCategory()),
                quantity > 0,
                quantity
        );
    }

    @Transactional(readOnly = true)
    public List<ProductCardResponse> getFeatured() {
        List<Product> products = productRepository.findTop8ByFeaturedTrueOrderByIdDesc();

        return toCards(products);
    }

    // The category menu of the store = the same tree we built in the Category module
    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategories() {
        return categoryService.getTree();
    }

    // ---------- helpers ----------

    // page/size/sort coming from the URL -> Pageable
    private Pageable buildPageable(int page, int size, String sort) {

        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be >= 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "size must be between 1 and " + MAX_PAGE_SIZE);
        }

        Sort sortBy = switch (sort) {
            case "newest" -> Sort.by("id").descending();
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "name" -> Sort.by("name").ascending();
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "sort must be one of: newest, price_asc, price_desc, name");
        };

        return PageRequest.of(page, size, sortBy);
    }

    // Walks down the tree: the category itself + children + grandchildren ...
    // public -> also used by the Search module
    @Transactional(readOnly = true)
    public Set<Long> collectCategoryAndDescendantIds(Long categoryId) {

        Category root = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found: " + categoryId
                ));

        Set<Long> ids = new HashSet<>();
        Deque<Category> toVisit = new ArrayDeque<>();
        toVisit.push(root);

        while (!toVisit.isEmpty()) {
            Category current = toVisit.pop();
            ids.add(current.getId());
            current.getChildren().forEach(toVisit::push);
        }

        return ids;
    }

    // Walks up the tree: Sneakers -> Shoes -> Men, then reversed to Men > Shoes > Sneakers
    private List<BreadcrumbItem> buildBreadcrumb(Category category) {
        List<BreadcrumbItem> path = new ArrayList<>();

        Category current = category;
        while (current != null) {
            path.add(new BreadcrumbItem(current.getId(), current.getName()));
            current = current.getParent();
        }

        Collections.reverse(path);
        return path;
    }

    private PageResponse<ProductCardResponse> toCardPage(Page<Product> page) {
        Map<Long, Integer> quantities = loadQuantities(page.getContent());

        return PageResponse.from(page, product -> toCard(product, quantities));
    }

    private List<ProductCardResponse> toCards(List<Product> products) {
        Map<Long, Integer> quantities = loadQuantities(products);

        return products.stream()
                .map(product -> toCard(product, quantities))
                .toList();
    }

    // One query for the stock of all products on the page (instead of one query per product)
    private Map<Long, Integer> loadQuantities(List<Product> products) {
        List<Long> ids = products.stream().map(Product::getId).toList();

        return ids.isEmpty() ? Map.of() : inventoryService.getQuantities(ids);
    }

    private ProductCardResponse toCard(Product product, Map<Long, Integer> quantities) {
        List<String> images = product.getImageUrls();
        String mainImage = images.isEmpty() ? null : images.get(0);

        return new ProductCardResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getBrand(),
                mainImage,
                product.getCategory().getId(),
                product.getCategory().getName(),
                quantities.getOrDefault(product.getId(), 0) > 0
        );
    }
}
