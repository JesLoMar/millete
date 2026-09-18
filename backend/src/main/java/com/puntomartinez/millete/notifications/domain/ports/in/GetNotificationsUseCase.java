package com.puntomartinez.millete.notifications.domain.ports.in;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.domain.model.PaginatedNotifications;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetNotificationsUseCase {

    List<Notification> getUserNotifications(
            UUID userId,
            int limit
    );

    PaginatedNotifications getUserNotificationsPage(
            UUID userId,
            int page,
            int size
    );

    long getUnreadCount(UUID userId);

    Optional<Notification> findUserNotificationByTypeAndMetadataValue(
            UUID userId,
            NotificationType type,
            String metadataKey,
            String metadataValue
    );
}