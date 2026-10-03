package com.puntomartinez.millete.notifications.domain.ports.in;

import java.util.List;
import java.util.UUID;

public interface ReconcileSystemNotificationsUseCase {

    void reconcile(
            UUID userId,
            String source,
            String title,
            List<SystemNotificationIssue> issues
    );

    record SystemNotificationIssue(
            String key,
            String code,
            String resourceId,
            String severity,
            String message
    ) {
    }
}