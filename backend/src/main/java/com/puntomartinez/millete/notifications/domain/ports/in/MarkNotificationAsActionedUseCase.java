package com.puntomartinez.millete.notifications.domain.ports.in;

import java.util.UUID;

public interface MarkNotificationAsActionedUseCase {

    void markAsActioned(UUID userId, UUID notificationId);
}
