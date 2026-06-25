package com.puntomartinez.millete.notifications.application.services;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.domain.ports.in.CreateNotificationUseCase;
import com.puntomartinez.millete.notifications.domain.ports.out.NotificationRepository;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private final UUID userId = UUID.randomUUID();
    private final UUID notificationId = UUID.randomUUID();

    @Test
    void shouldCreateNotification() {
        CreateNotificationUseCase.CreateNotificationCommand command =
                new CreateNotificationUseCase.CreateNotificationCommand(
                        userId,
                        NotificationType.GOAL_INVITATION,
                        "Nueva invitación",
                        "Te han invitado a una meta",
                        Map.of("goalId", UUID.randomUUID().toString()),
                        true,
                        LocalDateTime.now().plusDays(7)
                );

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Notification result = notificationService.create(command);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getType()).isEqualTo(NotificationType.GOAL_INVITATION);
        assertThat(result.getTitle()).isEqualTo("Nueva invitación");
        assertThat(result.isRead()).isFalse();
        assertThat(result.isActionRequired()).isTrue();
        assertThat(result.isActive()).isTrue();
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void shouldReturnUserNotificationsOrdered() {
        Notification n1 = createNotification(userId, "Nueva invitación 1", LocalDateTime.now().minusHours(2));
        Notification n2 = createNotification(userId, "Nueva invitación 2", LocalDateTime.now());

        when(notificationRepository.findActiveByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(n2, n1));

        List<Notification> result = notificationService.getUserNotifications(userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Nueva invitación 2");
    }

    @Test
    void shouldReturnUnreadCount() {
        when(notificationRepository.countUnreadByUserId(userId)).thenReturn(5L);

        long count = notificationService.getUnreadCount(userId);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    void shouldMarkNotificationAsRead() {
        Notification notification = createNotification(userId, "Test", LocalDateTime.now());

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.markAsRead(userId, notificationId);

        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void shouldNotAllowMarkAsReadFromOtherUser() {
        Notification notification = createNotification(UUID.randomUUID(), "Test", LocalDateTime.now());

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        assertThatExceptionOfType(ForbiddenOperationException.class)
                .isThrownBy(() -> notificationService.markAsRead(userId, notificationId));
    }

    @Test
    void shouldThrowWhenMarkingNonExistentNotification() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> notificationService.markAsRead(userId, notificationId));
    }

    @Test
    void shouldSoftDeleteNotification() {
        Notification notification = createNotification(userId, "Test", LocalDateTime.now());

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.delete(userId, notificationId);

        assertThat(notification.isActive()).isFalse();
        verify(notificationRepository).save(notification);
    }

    private Notification createNotification(UUID ownerId, String title, LocalDateTime createdAt) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setUserId(ownerId);
        notification.setType(NotificationType.GOAL_INVITATION);
        notification.setTitle(title);
        notification.setMessage("Test message");
        notification.setMetadata(Map.of());
        notification.setRead(false);
        notification.setActionRequired(true);
        notification.setCreatedAt(createdAt);
        notification.setActive(true);
        return notification;
    }
}
