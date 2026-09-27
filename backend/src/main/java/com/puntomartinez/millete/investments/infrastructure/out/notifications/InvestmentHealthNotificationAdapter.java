package com.puntomartinez.millete.investments.infrastructure.out.notifications;

import com.puntomartinez.millete.investments.domain.ports.in.InvestmentUseCases.HealthIssue;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentHealthNotificationPort;
import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.domain.ports.out.NotificationRepository;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public final class InvestmentHealthNotificationAdapter implements InvestmentHealthNotificationPort {
    private static final String SOURCE = "investments-health";
    private static final String TITLE = "Aviso de salud de inversiones";

    private final NotificationRepository notifications;
    private final TimeProvider time;

    public InvestmentHealthNotificationAdapter(NotificationRepository notifications, TimeProvider time) {
        this.notifications = notifications;
        this.time = time;
    }

    @Override
    public void reconcile(UUID userId, List<HealthIssue> issues) {
        Map<String, HealthIssue> current = new TreeMap<>();
        for (HealthIssue issue : issues) current.put(issue.key(), issue);

        List<Notification> active = notifications
                .findActiveByUserIdAndTypeAndMetadataValueOrderByCreatedAtDesc(
                        userId, NotificationType.SYSTEM, "source", SOURCE);
        Map<String, Notification> canonical = new HashMap<>();
        for (Notification notification : active) {
            Map<String, Object> metadata = notification.getMetadata();
            String key = metadata == null ? null : Objects.toString(metadata.get("issueKey"), null);
            if (key == null || !current.containsKey(key)) {
                notification.softDelete();
                notifications.save(notification);
                continue;
            }

            Notification existing = canonical.putIfAbsent(key, notification);
            if (existing != null) {
                notification.softDelete();
                notifications.save(notification);
            }
        }

        for (HealthIssue issue : current.values()) {
            Map<String, Object> metadata = metadata(issue);
            Notification existing = canonical.get(issue.key());
            if (existing == null) {
                notifications.save(Notification.create(time, userId, NotificationType.SYSTEM,
                        TITLE, issue.message(), metadata, false, null));
            } else if (existing.updateDetails(TITLE, issue.message(), metadata)) {
                notifications.save(existing);
            }
        }
    }

    private Map<String, Object> metadata(HealthIssue issue) {
        return Map.of("source", SOURCE, "issueKey", issue.key(), "code", issue.code(),
                "resourceId", issue.resourceId(), "severity", issue.severity());
    }
}
