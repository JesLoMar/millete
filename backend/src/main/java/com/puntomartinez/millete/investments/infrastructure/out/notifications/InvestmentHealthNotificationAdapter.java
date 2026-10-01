package com.puntomartinez.millete.investments.infrastructure.out.notifications;

import com.puntomartinez.millete.investments.domain.ports.in.CheckInvestmentHealthUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentHealthNotificationPort;
import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.domain.ports.out.NotificationRepository;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

@Component
public final class InvestmentHealthNotificationAdapter
        implements InvestmentHealthNotificationPort {

    private static final String SOURCE =
            "investments-health";

    private static final String TITLE =
            "Aviso de salud de inversiones";

    private final NotificationRepository notifications;
    private final TimeProvider time;

    public InvestmentHealthNotificationAdapter(
            NotificationRepository notifications,
            TimeProvider time
    ) {
        this.notifications = notifications;
        this.time = time;
    }

    @Override
    public void reconcile(
            UUID userId,
            List<CheckInvestmentHealthUseCase.HealthIssue> issues
    ) {
        Map<String, CheckInvestmentHealthUseCase.HealthIssue> current =
                new TreeMap<>();

        for (CheckInvestmentHealthUseCase.HealthIssue issue : issues) {
            current.put(
                    issueKey(issue),
                    issue
            );
        }

        List<Notification> active =
                notifications
                        .findActiveByUserIdAndTypeAndMetadataValueOrderByCreatedAtDesc(
                                userId,
                                NotificationType.SYSTEM,
                                "source",
                                SOURCE
                        );

        Map<String, Notification> canonical =
                new HashMap<>();

        for (Notification notification : active) {
            Map<String, Object> metadata =
                    notification.getMetadata();

            String key =
                    metadata == null
                            ? null
                            : Objects.toString(
                                    metadata.get("issueKey"),
                                    null
                            );

            if (key == null
                    || !current.containsKey(key)) {

                notification.softDelete();
                notifications.save(notification);
                continue;
            }

            Notification existing =
                    canonical.putIfAbsent(
                            key,
                            notification
                    );

            if (existing != null) {
                notification.softDelete();
                notifications.save(notification);
            }
        }

        for (
                CheckInvestmentHealthUseCase.HealthIssue issue
                : current.values()
        ) {
            String key =
                    issueKey(issue);

            Map<String, Object> metadata =
                    metadata(issue, key);

            Notification existing =
                    canonical.get(key);

            if (existing == null) {
                notifications.save(
                        Notification.create(
                                time,
                                userId,
                                NotificationType.SYSTEM,
                                TITLE,
                                issue.message(),
                                metadata,
                                false,
                                null
                        )
                );

                continue;
            }

            if (existing.updateDetails(
                    TITLE,
                    issue.message(),
                    metadata
            )) {
                notifications.save(
                        existing
                );
            }
        }
    }

    private String issueKey(
            CheckInvestmentHealthUseCase.HealthIssue issue
    ) {
        return issue.code()
                + ":"
                + String.valueOf(
                        issue.resourceId()
                );
    }

    private Map<String, Object> metadata(
            CheckInvestmentHealthUseCase.HealthIssue issue,
            String issueKey
    ) {
        return Map.of(
                "source",
                SOURCE,
                "issueKey",
                issueKey,
                "code",
                issue.code(),
                "resourceId",
                issue.resourceId(),
                "severity",
                issue.severity()
        );
    }
}