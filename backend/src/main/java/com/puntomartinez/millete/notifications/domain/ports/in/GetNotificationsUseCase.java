package com.puntomartinez.millete.notifications.domain.ports.in;

import com.puntomartinez.millete.notifications.domain.model.Notification;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface GetNotificationsUseCase {

    List<Notification> getUserNotifications(UUID userId, int limit);

    PaginatedNotifications getUserNotificationsPage(UUID userId, int page, int size);

    long getUnreadCount(UUID userId);

    Optional<Notification> findUserNotificationByTypeAndMetadataValue(
        UUID userId,
        String type,
        String metadataKey,
        String metadataValue
);

    record PaginatedNotifications(
            List<Notification> content,
            int currentPage,
            int totalPages,
            long totalElements,
            int size,
            boolean first,
            boolean last) {
    }
}