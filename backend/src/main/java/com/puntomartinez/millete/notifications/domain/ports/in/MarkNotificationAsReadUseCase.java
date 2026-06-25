package com.puntomartinez.millete.notifications.domain.ports.in;

import java.util.UUID;

public interface MarkNotificationAsReadUseCase {

    void markAsRead(UUID userId, UUID notificationId);
}
