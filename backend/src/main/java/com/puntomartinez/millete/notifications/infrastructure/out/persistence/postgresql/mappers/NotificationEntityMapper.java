package com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.entity.NotificationEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationEntityMapper {

    public Notification toDomain(NotificationEntity entity) {
        return Notification.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getType(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getMetadata(),
                entity.isRead(),
                entity.isActionRequired(),
                entity.getActionedAt(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.isActive()
        );
    }

    public NotificationEntity toEntity(Notification notification) {
        NotificationEntity entity = new NotificationEntity();

        entity.setId(notification.getId());
        entity.setUserId(notification.getUserId());
        entity.setType(notification.getType());
        entity.setTitle(notification.getTitle());
        entity.setMessage(notification.getMessage());
        entity.setMetadata(notification.getMetadata());
        entity.setRead(notification.isRead());
        entity.setActionRequired(notification.isActionRequired());
        entity.setActionedAt(notification.getActionedAt());
        entity.setCreatedAt(notification.getCreatedAt());
        entity.setExpiresAt(notification.getExpiresAt());
        entity.setActive(notification.isActive());

        return entity;
    }
}