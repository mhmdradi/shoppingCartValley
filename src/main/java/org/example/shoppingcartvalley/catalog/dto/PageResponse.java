package org.example.shoppingcartvalley.catalog.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

// Our own JSON shape for a page of results.
// We don't return Spring's Page object directly: its JSON format is not stable between versions.
//   <T> = generic type -> the same class works for a page of products, orders, users...
public record PageResponse<T>(
        List<T> content,
        int page,             // current page number (starts at 0)
        int size,             // items per page
        long totalElements,   // total items in the DB that match
        int totalPages,
        boolean last          // true -> no next page
) {

    // Converts a Page<Entity> into a PageResponse<Dto> using the given mapper function
    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
