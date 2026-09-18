package com.puntomartinez.millete.notifications.application.services;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.domain.model.PaginatedNotifications;
import com.puntomartinez.millete.notifications.domain.ports.in.*;
import com.puntomartinez.millete.notifications.domain.ports.out.NotificationRepository;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService implements
        CreateNotificationUseCase,
        GetNotificationsUseCase,
        MarkNotificationAsReadUseCase,
        MarkNotificationAsActionedUseCase,
        DeleteNotificationUseCase {

    private static final int DEFAULT_NOTIFICATION_LIMIT = 25;
    private static final int MAX_NOTIFICATION_LIMIT = 100;
    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationRepository notificationRepository;

    @Override
    public Notification create(CreateNotificationCommand command) {
        Notification notification = Notification.create(
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
                        LocalDateTime.now()
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
                        LocalDateTime.now()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository
                .countUnreadActiveAndNotExpiredByUserId(
                        userId,
                        LocalDateTime.now()
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
                        LocalDateTime.now()
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
                        LocalDateTime.now()
                )
                .orElse(null);

        if (notification == null) {
            return false;
        }

        boolean changed = notification.markAsActioned();

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
                        LocalDateTime.now()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notificación no encontrada."
                        )
                );

        notification.softDelete();
        notificationRepository.save(notification);
    }

    private void validatePaginationParams(int page, int size) {
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
                            + MAX_PAGE_SIZE + "."
            );
        }
    }
}