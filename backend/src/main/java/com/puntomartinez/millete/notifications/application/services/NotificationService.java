package com.puntomartinez.millete.notifications.application.services;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.ports.in.*;
import com.puntomartinez.millete.notifications.domain.ports.out.NotificationRepository;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

    private final NotificationRepository notificationRepository;

    @Override
    public Notification create(CreateNotificationCommand command) {
        Notification notification = Notification.builder()
                .id(UUID.randomUUID())
                .userId(command.userId())
                .type(command.type())
                .title(command.title())
                .message(command.message())
                .metadata(command.metadata())
                .read(false)
                .actionRequired(command.actionRequired())
                .createdAt(LocalDateTime.now())
                .expiresAt(command.expiresAt())
                .active(true)
                .build();

        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(UUID userId) {
        return notificationRepository.findActiveByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Override
    public void markAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada"));

        if (!notification.getUserId().equals(userId)) {
            throw new ForbiddenOperationException("No tienes permiso para modificar esta notificación");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Override
    public void markAsActioned(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada"));

        if (!notification.getUserId().equals(userId)) {
            throw new ForbiddenOperationException("No tienes permiso para modificar esta notificación");
        }

        notification.markAsActioned();
        notificationRepository.save(notification);
    }

    @Override
    public void delete(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada"));

        if (!notification.getUserId().equals(userId)) {
            throw new ForbiddenOperationException("No tienes permiso para eliminar esta notificación");
        }

        notification.softDelete();
        notificationRepository.save(notification);
    }
}
