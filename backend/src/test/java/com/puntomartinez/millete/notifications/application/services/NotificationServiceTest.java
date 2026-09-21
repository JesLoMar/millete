package com.puntomartinez.millete.notifications.application.services;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.domain.model.PaginatedNotifications;
import com.puntomartinez.millete.notifications.domain.ports.in.CreateNotificationUseCase.CreateNotificationCommand;
import com.puntomartinez.millete.notifications.domain.ports.out.NotificationRepository;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService")
class NotificationServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID NOTIFICATION_ID = UUID.randomUUID();

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification validNotification() {
        return Notification.create(
                USER_ID,
                NotificationType.GOAL_INVITATION,
                "Nueva invitación",
                "Te han invitado a una meta",
                Map.of("goalId", UUID.randomUUID().toString()),
                true,
                LocalDateTime.now().plusDays(7)
        );
    }

    @Nested
    @DisplayName("create")
    class Create {
        @Test
        @DisplayName("Should create and save notification")
        void shouldCreateAndSaveNotification() {
            CreateNotificationCommand command = new CreateNotificationCommand(
                    USER_ID,
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

            assertThat(result.getUserId()).isEqualTo(USER_ID);
            assertThat(result.getType()).isEqualTo(NotificationType.GOAL_INVITATION);
            assertThat(result.getTitle()).isEqualTo("Nueva invitación");
            assertThat(result.isRead()).isFalse();
            assertThat(result.isActionRequired()).isTrue();
            assertThat(result.isActive()).isTrue();
            verify(notificationRepository).save(any(Notification.class));
        }
    }

    @Nested
    @DisplayName("getUserNotifications")
    class GetUserNotifications {
        @Test
        @DisplayName("Should use default limit when limit is zero or negative")
        void shouldUseDefaultLimitWhenZeroOrNegative() {
            when(notificationRepository.findActiveAndNotExpiredByUserIdOrderByCreatedAtDesc(
                    eq(USER_ID), eq(25), any(LocalDateTime.class)
            )).thenReturn(List.of());

            notificationService.getUserNotifications(USER_ID, 0);

            verify(notificationRepository)
                    .findActiveAndNotExpiredByUserIdOrderByCreatedAtDesc(
                            eq(USER_ID), eq(25), any(LocalDateTime.class)
                    );
        }

        @Test
        @DisplayName("Should cap limit at 100 when exceeding max")
        void shouldCapLimitAtMax() {
            when(notificationRepository.findActiveAndNotExpiredByUserIdOrderByCreatedAtDesc(
                    eq(USER_ID), eq(100), any(LocalDateTime.class)
            )).thenReturn(List.of());

            notificationService.getUserNotifications(USER_ID, 500);

            verify(notificationRepository)
                    .findActiveAndNotExpiredByUserIdOrderByCreatedAtDesc(
                            eq(USER_ID), eq(100), any(LocalDateTime.class)
                    );
        }

        @Test
        @DisplayName("Should use provided limit when within bounds")
        void shouldUseProvidedLimitWhenWithinBounds() {
            Notification n1 = validNotification();
            when(notificationRepository.findActiveAndNotExpiredByUserIdOrderByCreatedAtDesc(
                    eq(USER_ID), eq(10), any(LocalDateTime.class)
            )).thenReturn(List.of(n1));

            List<Notification> result = notificationService.getUserNotifications(USER_ID, 10);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getUserNotificationsPage")
    class GetUserNotificationsPage {
        @Test
        @DisplayName("Should return paginated notifications")
        void shouldReturnPaginatedNotifications() {
            PaginatedNotifications expected = new PaginatedNotifications(
                    List.of(validNotification()), 0, 1, 1, 25, true, true
            );
            when(notificationRepository.findActiveAndNotExpiredByUserIdPaginated(
                    eq(USER_ID), eq(0), eq(25), any(LocalDateTime.class)
            )).thenReturn(expected);

            PaginatedNotifications result =
                    notificationService.getUserNotificationsPage(USER_ID, 0, 25);

            assertThat(result.content()).hasSize(1);
            assertThat(result.currentPage()).isZero();
        }

        @Test
        @DisplayName("Should reject negative page")
        void shouldRejectNegativePage() {
            assertThatThrownBy(() ->
                    notificationService.getUserNotificationsPage(USER_ID, -1, 25)
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject zero size")
        void shouldRejectZeroSize() {
            assertThatThrownBy(() ->
                    notificationService.getUserNotificationsPage(USER_ID, 0, 0)
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject size exceeding 100")
        void shouldRejectSizeExceedingMax() {
            assertThatThrownBy(() ->
                    notificationService.getUserNotificationsPage(USER_ID, 0, 101)
            ).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("getUnreadCount")
    class GetUnreadCount {
        @Test
        @DisplayName("Should return unread count")
        void shouldReturnUnreadCount() {
            when(notificationRepository.countUnreadActiveAndNotExpiredByUserId(
                    eq(USER_ID), any(LocalDateTime.class)
            )).thenReturn(5L);

            long count = notificationService.getUnreadCount(USER_ID);

            assertThat(count).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("markAsRead")
    class MarkAsRead {
        @Test
        @DisplayName("Should mark as read and save when unread")
        void shouldMarkAsReadAndSave() {
            Notification notification = validNotification();
            when(notificationRepository.findActiveAndNotExpiredByIdAndUserId(
                    eq(NOTIFICATION_ID), eq(USER_ID), any(LocalDateTime.class)
            )).thenReturn(Optional.of(notification));

            notificationService.markAsRead(USER_ID, NOTIFICATION_ID);

            assertThat(notification.isRead()).isTrue();
            verify(notificationRepository).save(notification);
        }

        @Test
        @DisplayName("Should not save when already read")
        void shouldNotSaveWhenAlreadyRead() {
            Notification notification = validNotification();
            notification.markAsRead();
            when(notificationRepository.findActiveAndNotExpiredByIdAndUserId(
                    eq(NOTIFICATION_ID), eq(USER_ID), any(LocalDateTime.class)
            )).thenReturn(Optional.of(notification));

            notificationService.markAsRead(USER_ID, NOTIFICATION_ID);

            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(notificationRepository.findActiveAndNotExpiredByIdAndUserId(
                    eq(NOTIFICATION_ID), eq(USER_ID), any(LocalDateTime.class)
            )).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    notificationService.markAsRead(USER_ID, NOTIFICATION_ID)
            ).isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when notification belongs to other user")
        void shouldThrowWhenNotificationBelongsToOtherUser() {
            // El repositorio filtra por userId, así que si la notificación
            // es de otro usuario, simplemente no la encuentra
            when(notificationRepository.findActiveAndNotExpiredByIdAndUserId(
                    eq(NOTIFICATION_ID), eq(USER_ID), any(LocalDateTime.class)
            )).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    notificationService.markAsRead(USER_ID, NOTIFICATION_ID)
            ).isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("markAsActioned")
    class MarkAsActioned {
        @Test
        @DisplayName("Should mark as actioned and return true")
        void shouldMarkAsActionedAndReturnTrue() {
            Notification notification = validNotification();
            when(notificationRepository.findActiveAndNotExpiredByIdAndUserId(
                    eq(NOTIFICATION_ID), eq(USER_ID), any(LocalDateTime.class)
            )).thenReturn(Optional.of(notification));

            boolean result = notificationService.markAsActioned(USER_ID, NOTIFICATION_ID);

            assertThat(result).isTrue();
            assertThat(notification.getActionedAt()).isNotNull();
            verify(notificationRepository).save(notification);
        }

        @Test
        @DisplayName("Should return false when not found")
        void shouldReturnFalseWhenNotFound() {
            when(notificationRepository.findActiveAndNotExpiredByIdAndUserId(
                    eq(NOTIFICATION_ID), eq(USER_ID), any(LocalDateTime.class)
            )).thenReturn(Optional.empty());

            boolean result = notificationService.markAsActioned(USER_ID, NOTIFICATION_ID);

            assertThat(result).isFalse();
            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should return true without saving when already actioned")
        void shouldReturnTrueWithoutSavingWhenAlreadyActioned() {
            Notification notification = validNotification();
            notification.markAsActioned();
            when(notificationRepository.findActiveAndNotExpiredByIdAndUserId(
                    eq(NOTIFICATION_ID), eq(USER_ID), any(LocalDateTime.class)
            )).thenReturn(Optional.of(notification));

            boolean result = notificationService.markAsActioned(USER_ID, NOTIFICATION_ID);

            assertThat(result).isTrue();
            verify(notificationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("Should soft delete and save")
        void shouldSoftDeleteAndSave() {
            Notification notification = validNotification();
            when(notificationRepository.findActiveAndNotExpiredByIdAndUserId(
                    eq(NOTIFICATION_ID), eq(USER_ID), any(LocalDateTime.class)
            )).thenReturn(Optional.of(notification));

            notificationService.delete(USER_ID, NOTIFICATION_ID);

            assertThat(notification.isActive()).isFalse();
            verify(notificationRepository).save(notification);
        }

        @Test
        @DisplayName("Should throw when not found")
        void shouldThrowWhenNotFound() {
            when(notificationRepository.findActiveAndNotExpiredByIdAndUserId(
                    eq(NOTIFICATION_ID), eq(USER_ID), any(LocalDateTime.class)
            )).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    notificationService.delete(USER_ID, NOTIFICATION_ID)
            ).isInstanceOf(ResourceNotFoundException.class);
        }
    }
}