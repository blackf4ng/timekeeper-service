package org.timekeeper.model.transform;

import org.timekeeper.model.Page;

import java.util.function.Function;

/**
 * Transforms into the internal Page representation
 */
public final class PageTransform {

    /**
     * Helper function that transforms from the Spring Page into the internal Page representation.
     * As the value in the Spring Page is likely to be the database representation of the domain object,
     * it also provides a mapper parameter for defining the database to internal transformation to use.
     *
     * @param from The Spring Page to transform from
     * @param valueTransform The function to use to transform from the database model within the Spring Page into an internal model
     * @param <T> The class of the item in the Spring Page
     * @param <R> The class of the internal representation of an item
     * @return An internal Page that represents the Spring Page
     */
    public static <T, R> Page<R> apply(org.springframework.data.domain.Page<T> from, Function<T, R> valueTransform) {
        return Page.<R>builder()
            .data(
                from.getContent().stream()
                    .map(valueTransform)
                    .toList()
            ).page(from.getPageable().getPageNumber())
            .totalElements(from.getTotalElements())
            .totalPages(from.getTotalPages())
            .build();
    }

}
