package com.puntomartinez.millete.notifications.domain.ports.out;

import com.puntomartinez.millete.notifications.domain.model.Notification;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    List<Notification> findActiveByUserIdOrderByCreatedAtDesc(UUID userId, int limit);

    Page<Notification> findActiveByUserIdPaginated(UUID userId, int page, int size);

    long countUnreadByUserId(UUID userId);

    List<Notification> findActiveByUserIdAndTypeAndMetadataValue(UUID userId, String type, String metadataKey, String metadataValue);
}
