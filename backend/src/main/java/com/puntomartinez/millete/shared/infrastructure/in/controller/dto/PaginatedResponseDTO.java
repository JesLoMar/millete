package com.puntomartinez.millete.shared.infrastructure.in.controller.dto;

import java.util.List;

public record PaginatedResponseDTO<T>(
        List<T> content,
        int currentPage,
        int totalPages,
        long totalElements,
        int size,
        boolean first,
        boolean last
) {
}
