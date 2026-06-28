package com.puntomartinez.millete.notifications.domain.ports.in;

import com.puntomartinez.millete.notifications.domain.model.Notification;

import java.util.List;
import java.util.UUID;

public interface GetNotificationsUseCase {

    List<Notification> getUserNotifications(UUID userId);

    long getUnreadCount(UUID userId);
}
