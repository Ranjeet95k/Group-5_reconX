package com.dbtraining.reconx.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * TICKET-ADV063 — Wrapper that flattens Spring Data Page<T>
 * into a stable JSON response shape.
 */
public record PagedResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {

    public static <S, T> PagedResponse<T> from(
            Page<S> src,
            java.util.function.Function<S, T> mapper
    ) {
        return new PagedResponse<>(
                src.getContent()
                        .stream()
                        .map(mapper)
                        .toList(),
                src.getNumber(),
                src.getSize(),
                src.getTotalElements(),
                src.getTotalPages(),
                src.isLast()
        );
    }
}