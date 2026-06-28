package com.puntomartinez.millete.notifications.domain.ports.in;

import java.util.UUID;

public interface DeleteNotificationUseCase {

    void delete(UUID userId, UUID notificationId);
}
