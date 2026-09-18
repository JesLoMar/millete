package com.puntomartinez.millete.notifications.domain.model;

import java.util.List;

public record PaginatedNotifications(
        List<Notification> content,
        int currentPage,
        int totalPages,
        long totalElements,
        int size,
        boolean first,
        boolean last
) {
}