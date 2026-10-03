package com.puntomartinez.millete.notifications.application.services;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.domain.model.PaginatedNotifications;
import com.puntomartinez.millete.notifications.domain.ports.in.CreateNotificationUseCase.CreateNotificationCommand;
import com.puntomartinez.millete.notifications.domain.ports.in.CreateNotificationUseCase;
import com.puntomartinez.millete.notifications.domain.ports.in.DeleteNotificationUseCase;
import com.puntomartinez.millete.notifications.domain.ports.in.GetNotificationsUseCase;
import com.puntomartinez.millete.notifications.domain.ports.in.MarkNotificationAsActionedUseCase;
import com.puntomartinez.millete.notifications.domain.ports.in.MarkNotificationAsReadUseCase;
import com.puntomartinez.millete.notifications.domain.ports.in.ReconcileSystemNotificationsUseCase;
import com.puntomartinez.millete.notifications.domain.ports.in.ReconcileSystemNotificationsUseCase.SystemNotificationIssue;
import com.puntomartinez.millete.notifications.domain.ports.out.NotificationRepository;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService implements
        CreateNotificationUseCase,
        GetNotificationsUseCase,
        MarkNotificationAsReadUseCase,
        MarkNotificationAsActionedUseCase,
        DeleteNotificationUseCase,
        ReconcileSystemNotificationsUseCase {

    private static final int DEFAULT_NOTIFICATION_LIMIT = 25;
    private static final int MAX_NOTIFICATION_LIMIT = 100;
    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationRepository notificationRepository;
    private final TimeProvider timeProvider;

    @Override
    public Notification create(CreateNotificationCommand command) {
        Notification notification = Notification.create(
                timeProvider,
                command.userId(),
                command.type(),
                command.title(),
                command.message(),
                command.metadata(),
                command.actionRequired(),
                command.expiresAt()
        );

        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(
            UUID userId,
            int limit
    ) {
        int safeLimit = limit <= 0
                ? DEFAULT_NOTIFICATION_LIMIT
                : Math.min(limit, MAX_NOTIFICATION_LIMIT);

        return notificationRepository
                .findActiveAndNotExpiredByUserIdOrderByCreatedAtDesc(
                        userId,
                        safeLimit,
                        timeProvider.now()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedNotifications getUserNotificationsPage(
            UUID userId,
            int page,
            int size
    ) {
        validatePaginationParams(page, size);

        return notificationRepository
                .findActiveAndNotExpiredByUserIdPaginated(
                        userId,
                        page,
                        size,
                        timeProvider.now()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository
                .countUnreadActiveAndNotExpiredByUserId(
                        userId,
                        timeProvider.now()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Notification> findUserNotificationByTypeAndMetadataValue(
            UUID userId,
            NotificationType type,
            String metadataKey,
            String metadataValue
    ) {
        return notificationRepository
                .findActiveByUserIdAndTypeAndMetadataValue(
                        userId,
                        type,
                        metadataKey,
                        metadataValue
                );
    }

    @Override
    public void markAsRead(
            UUID userId,
            UUID notificationId
    ) {
        Notification notification = notificationRepository
                .findActiveAndNotExpiredByIdAndUserId(
                        notificationId,
                        userId,
                        timeProvider.now()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notificación no encontrada."
                        )
                );

        boolean changed = notification.markAsRead();

        if (changed) {
            notificationRepository.save(notification);
        }
    }

    @Override
    public boolean markAsActioned(
            UUID userId,
            UUID notificationId
    ) {
        Notification notification = notificationRepository
                .findActiveAndNotExpiredByIdAndUserId(
                        notificationId,
                        userId,
                        timeProvider.now()
                )
                .orElse(null);

        if (notification == null) {
            return false;
        }

        boolean changed =
                notification.markAsActioned(
                        timeProvider
                );

        if (changed) {
            notificationRepository.save(notification);
        }

        return true;
    }

    @Override
    public void delete(
            UUID userId,
            UUID notificationId
    ) {
        Notification notification = notificationRepository
                .findActiveAndNotExpiredByIdAndUserId(
                        notificationId,
                        userId,
                        timeProvider.now()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notificación no encontrada."
                        )
                );

        notification.softDelete();

        notificationRepository.save(notification);
    }

    @Override
    public void reconcile(
            UUID userId,
            String source,
            String title,
            List<SystemNotificationIssue> issues
    ) {
        validateReconciliationInput(
                userId,
                source,
                issues
        );

        Map<String, SystemNotificationIssue> current =
                new TreeMap<>();

        for (SystemNotificationIssue issue : issues) {
            if (issue == null) {
                throw new InvalidInputException(
                        "Una incidencia de notificación no puede ser nula."
                );
            }

            current.put(
                    issue.key(),
                    issue
            );
        }

        List<Notification> active =
                notificationRepository
                        .findActiveByUserIdAndTypeAndMetadataValueOrderByCreatedAtDesc(
                                userId,
                                NotificationType.SYSTEM,
                                "source",
                                source
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
                notificationRepository.save(notification);
                continue;
            }

            Notification existing =
                    canonical.putIfAbsent(
                            key,
                            notification
                    );

            if (existing != null) {
                notification.softDelete();
                notificationRepository.save(notification);
            }
        }

        for (SystemNotificationIssue issue :
                current.values()) {

            String key =
                    issue.key();

            Map<String, Object> metadata =
                    metadata(
                            source,
                            issue
                    );

            Notification existing =
                    canonical.get(key);

            if (existing == null) {
                notificationRepository.save(
                        Notification.create(
                                timeProvider,
                                userId,
                                NotificationType.SYSTEM,
                                title,
                                issue.message(),
                                metadata,
                                false,
                                null
                        )
                );

                continue;
            }

            if (existing.updateDetails(
                    title,
                    issue.message(),
                    metadata
            )) {
                notificationRepository.save(
                        existing
                );
            }
        }
    }

    private Map<String, Object> metadata(
            String source,
            SystemNotificationIssue issue
    ) {
        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "source",
                source
        );

        metadata.put(
                "issueKey",
                issue.key()
        );

        if (issue.code() != null) {
            metadata.put(
                    "code",
                    issue.code()
            );
        }

        if (issue.resourceId() != null) {
            metadata.put(
                    "resourceId",
                    issue.resourceId()
            );
        }

        if (issue.severity() != null) {
            metadata.put(
                    "severity",
                    issue.severity()
            );
        }

        return metadata;
    }

    private void validateReconciliationInput(
            UUID userId,
            String source,
            List<SystemNotificationIssue> issues
    ) {
        if (userId == null) {
            throw new InvalidInputException(
                    "El userId es obligatorio."
            );
        }

        if (source == null
                || source.isBlank()) {
            throw new InvalidInputException(
                    "El source de la reconciliación es obligatorio."
            );
        }

        if (issues == null) {
            throw new InvalidInputException(
                    "La lista de incidencias es obligatoria."
            );
        }
    }

    private void validatePaginationParams(
            int page,
            int size
    ) {
        if (page < 0) {
            throw new InvalidInputException(
                    "La página debe ser mayor o igual que 0."
            );
        }

        if (size <= 0) {
            throw new InvalidInputException(
                    "El tamaño de página debe ser mayor que 0."
            );
        }

        if (size > MAX_PAGE_SIZE) {
            throw new InvalidInputException(
                    "El tamaño de página no puede superar "
                            + MAX_PAGE_SIZE
                            + "."
            );
        }
    }
}