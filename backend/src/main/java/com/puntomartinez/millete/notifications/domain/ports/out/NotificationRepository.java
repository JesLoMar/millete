package com.puntomartinez.millete.notifications.domain.ports.out;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.domain.model.PaginatedNotifications;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findActiveAndNotExpiredByIdAndUserId(
            UUID id,
            UUID userId,
            LocalDateTime now
    );

    List<Notification> findActiveAndNotExpiredByUserIdOrderByCreatedAtDesc(
            UUID userId,
            int limit,
            LocalDateTime now
    );

    PaginatedNotifications findActiveAndNotExpiredByUserIdPaginated(
            UUID userId,
            int page,
            int size,
            LocalDateTime now
    );

    long countUnreadActiveAndNotExpiredByUserId(
            UUID userId,
            LocalDateTime now
    );

    Optional<Notification> findActiveByUserIdAndTypeAndMetadataValue(
            UUID userId,
            NotificationType type,
            String metadataKey,
            String metadataValue
    );
}