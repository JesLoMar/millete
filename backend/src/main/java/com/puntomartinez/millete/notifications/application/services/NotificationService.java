package com.puntomartinez.millete.notifications.application.services;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.ports.in.*;
import com.puntomartinez.millete.notifications.domain.ports.out.NotificationRepository;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            int limit) {

        int safeLimit = limit <= 0
                ? DEFAULT_NOTIFICATION_LIMIT
                : Math.min(limit, MAX_NOTIFICATION_LIMIT);

        return notificationRepository
                .findActiveByUserIdOrderByCreatedAtDesc(
                        userId,
                        safeLimit
                );
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedNotifications getUserNotificationsPage(
            UUID userId,
            int page,
            int size) {

        return notificationRepository.findActiveByUserIdPaginated(
                userId,
                page,
                size
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Notification> findUserNotificationByTypeAndMetadataValue(
            UUID userId,
            String type,
            String metadataKey,
            String metadataValue) {

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
            UUID notificationId) {

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notificación no encontrada"
                                ));

        if (!notification.getUserId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "No tienes permiso para modificar esta notificación"
            );
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Override
    public void markAsActioned(
            UUID userId,
            UUID notificationId) {

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notificación no encontrada"
                                ));

        if (!notification.getUserId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "No tienes permiso para modificar esta notificación"
            );
        }

        notification.markAsActioned();
        notificationRepository.save(notification);
    }

    @Override
    public void delete(
            UUID userId,
            UUID notificationId) {

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notificación no encontrada"
                                ));

        if (!notification.getUserId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "No tienes permiso para eliminar esta notificación"
            );
        }

        notification.softDelete();
        notificationRepository.save(notification);
    }
}