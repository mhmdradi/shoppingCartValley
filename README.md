# ShoppingCartValley

A RESTful e-commerce backend built with **Spring Boot** and **Spring Data JPA**.
It is organized into feature modules. Each module has its own entity, repository, DTO, service and controller layers.

> Work in progress: built during backend training at ValleySoft.

## Tech Stack

- Java 17
- Spring Boot 4 (Web MVC, Data JPA, Validation)
- Hibernate
- H2 (in-memory database for development)
- Lombok
- Maven

## Modules

| Module | What it does |
|---|---|
| **Category** | CRUD for categories, with unlimited subcategories. Prevents cycles (a category cannot become a child of its own subcategory). Blocks deleting categories that still have subcategories or products. Tree endpoint. |
| **Product** | CRUD for products: price (`BigDecimal`), unique SKU, brand, multiple images and key/value attributes (`@ElementCollection`). Can mark products as featured. |
| **Inventory** | One stock record per product. Stock is increased and decreased with **atomic conditional SQL updates**, so stock never goes negative under concurrent orders. Includes availability checks and low-stock alerts. |
| **Catalog** | Read-only, customer-facing endpoints. Paginated and sorted product listing; filtering by category includes all subcategories. Product details with a breadcrumb. Featured products. |
| **Search** | Dynamic search with optional filters for keyword, category, price range, brand and availability, built with **JPA Specifications**. |

## Project Structure

```
src/main/java/org/example/shoppingcartvalley
├── category/    controller · dto · entity · repository · service
├── product/     controller · dto · entity · repository · service
├── inventory/   controller · dto · entity · repository · service
├── catalog/     controller · dto · service
└── search/      controller · dto · service · specification
```

## API Overview

### Admin

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/categories` | Create a category (`parentId` is optional) |
| GET | `/api/categories` | List all categories |
| GET | `/api/categories/{id}` | Get one category |
| PUT | `/api/categories/{id}` | Update a category |
| DELETE | `/api/categories/{id}` | Delete a category |
| GET | `/api/categories/tree` | Get the full category tree |
| GET | `/api/categories/{id}/children` | Get direct subcategories |
| POST | `/api/products` | Create a product |
| GET | `/api/products` | List all products |
| GET | `/api/products?categoryId=` | List products in one category |
| GET | `/api/products/{id}` | Get one product |
| PUT | `/api/products/{id}` | Update a product |
| DELETE | `/api/products/{id}` | Delete a product |
| PATCH | `/api/products/{id}/featured` | Mark or unmark a product as featured |
| GET | `/api/inventory/products/{productId}` | Get the stock of a product |
| PUT | `/api/inventory/products/{productId}` | Set the exact stock quantity |
| POST | `/api/inventory/products/{productId}/adjust` | Add or remove stock (`delta`) |
| GET | `/api/inventory/low-stock` | List products with low stock |

### Store front

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/catalog/products?categoryId=&page=&size=&sort=` | List products. `sort` can be `newest`, `price_asc`, `price_desc` or `name` |
| GET | `/api/catalog/products/{id}` | Product details |
| GET | `/api/catalog/featured` | Featured products |
| GET | `/api/catalog/categories` | Category menu |
| GET | `/api/catalog/search?keyword=&categoryId=&minPrice=&maxPrice=&brand=&inStock=` | Search products |
| GET | `/api/catalog/search/brands` | List all brands |

Errors return the matching HTTP status:

| Status | When |
|---|---|
| `400` | Validation failed |
| `404` | Resource not found |
| `409` | Conflict: duplicate SKU or name, not enough stock, or deleting a category that is still in use |

## Run Locally

```bash
./mvnw spring-boot:run
```

- **API:** `http://localhost:8081`
- **H2 console:** `http://localhost:8081/h2-console`
  - JDBC URL: `jdbc:h2:mem:shoppingcart`
  - User: `sa`
  - Password: empty

## Roadmap

- [x] Category management
- [x] Product management
- [x] Inventory
- [x] Catalog
- [x] Search
- [ ] Authentication (JWT), registration, users and roles
- [ ] Shopping cart, wishlist and customer profile
- [ ] Checkout, orders and payments
- [ ] Shipping, discounts and coupons
