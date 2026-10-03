package com.puntomartinez.millete.investments.infrastructure.out.notifications;

import com.puntomartinez.millete.investments.domain.ports.in.CheckInvestmentHealthUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentHealthNotificationPort;
import com.puntomartinez.millete.notifications.domain.ports.in.ReconcileSystemNotificationsUseCase;
import com.puntomartinez.millete.notifications.domain.ports.in.ReconcileSystemNotificationsUseCase.SystemNotificationIssue;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public final class InvestmentHealthNotificationAdapter
        implements InvestmentHealthNotificationPort {

    private static final String SOURCE =
            "investments-health";

    private static final String TITLE =
            "Aviso de salud de inversiones";

    private final ReconcileSystemNotificationsUseCase notifications;

    public InvestmentHealthNotificationAdapter(
            ReconcileSystemNotificationsUseCase notifications
    ) {
        this.notifications = notifications;
    }

    @Override
    public void reconcile(
            UUID userId,
            List<CheckInvestmentHealthUseCase.HealthIssue> issues
    ) {
        List<SystemNotificationIssue> notificationIssues =
                issues.stream()
                        .map(this::toNotificationIssue)
                        .toList();

        notifications.reconcile(
                userId,
                SOURCE,
                TITLE,
                notificationIssues
        );
    }

    private SystemNotificationIssue toNotificationIssue(
            CheckInvestmentHealthUseCase.HealthIssue issue
    ) {
        return new SystemNotificationIssue(
                issueKey(issue),
                issue.code(),
                issue.resourceId(),
                issue.severity(),
                issue.message()
        );
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
}