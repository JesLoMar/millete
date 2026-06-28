package com.puntomartinez.millete.notifications.domain.ports.in;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public interface CreateNotificationUseCase {

    Notification create(CreateNotificationCommand command);

    record CreateNotificationCommand(
            UUID userId,
            NotificationType type,
            String title,
            String message,
            Map<String, Object> metadata,
            boolean actionRequired,
            LocalDateTime expiresAt
    ) {}
}
