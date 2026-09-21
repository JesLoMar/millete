package com.puntomartinez.millete.notifications.application.services;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.domain.model.PaginatedNotifications;
import com.puntomartinez.millete.notifications.domain.ports.in.CreateNotificationUseCase;
import com.puntomartinez.millete.notifications.domain.ports.out.NotificationRepository;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService")
class NotificationServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification activeNotification() {
        return Notification.create(
                USER_ID,
                NotificationType.GOAL_INVITATION,
                "Goal invitation",
                "You have been invited",
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
            CreateNotificationUseCase.CreateNotificationCommand command =
                    new CreateNotificationUseCase.CreateNotificationCommand(
                            USER_ID,
                            NotificationType.GOAL_INVITATION,
                            "Goal invitation",
                            "You have been invited",
                            Map.of("goalId", UUID.randomUUID().toString()),
                            true,
                            LocalDateTime.now().plusDays(7)
                    );

            when(notificationRepository.save(any(Notification.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Notification result = notificationService.create(command);

            ArgumentCaptor<Notification> captor =
                    ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());

            Notification saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(USER_ID);
            assertThat(saved.getType()).isEqualTo(NotificationType.GOAL_INVITATION);
            assertThat(saved.isRead()).isFalse();
            assertThat(saved.isActive()).isTrue();
            assertThat(result).isSameAs(saved);
        }
    }

    @Nested
    @DisplayName("getUserNotifications")
    class GetUserNotifications {

        @Test
        @DisplayName("Should use default limit when limit is zero or negative")
        void shouldUseDefaultLimitWhenLimitIsZeroOrNegative() {
            Notification notification = activeNotification();
            when(notificationRepository
                    .findActiveAndNotExpiredByUserIdOrderByCreatedAtDesc(
                            eq(USER_ID), eq(25), any(LocalDateTime.class)
                    )
            ).thenReturn(List.of(notification));

            List<Notification> result =
                    notificationService.getUserNotifications(USER_ID, 0);

            assertThat(result).containsExactly(notification);
            verify(notificationRepository)
                    .findActiveAndNotExpiredByUserIdOrderByCreatedAtDesc(
                            eq(USER_ID), eq(25), any(LocalDateTime.class)
                    );
        }

        @Test
        @DisplayName("Should cap limit at 100 when exceeding max")
        void shouldCapLimitAtMaxWhenExceedingMax() {
            when(notificationRepository
                    .findActiveAndNotExpiredByUserIdOrderByCreatedAtDesc(
                            eq(USER_ID), eq(100), any(LocalDateTime.class)
                    )
            ).thenReturn(List.of());

            notificationService.getUserNotifications(USER_ID, 500);

            verify(notificationRepository)
                    .findActiveAndNotExpiredByUserIdOrderByCreatedAtDesc(
                            eq(USER_ID), eq(100), any(LocalDateTime.class)
                    );
        }

        @Test
        @DisplayName("Should use provided limit when within valid range")
        void shouldUseProvidedLimitWhenWithinValidRange() {
            when(notificationRepository
                    .findActiveAndNotExpiredByUserIdOrderByCreatedAtDesc(
                            eq(USER_ID), eq(10), any(LocalDateTime.class)
                    )
            ).thenReturn(List.of());

            notificationService.getUserNotifications(USER_ID, 10);

            verify(notificationRepository)
                    .findActiveAndNotExpiredByUserIdOrderByCreatedAtDesc(
                            eq(USER_ID), eq(10), any(LocalDateTime.class)
                    );
        }
    }

    @Nested
    @DisplayName("getUserNotificationsPage")
    class GetUserNotificationsPage {

        @Test
        @DisplayName("Should delegate to repository with valid pagination")
        void shouldDelegateToRepositoryWithValidPagination() {
            PaginatedNotifications expected = new PaginatedNotifications(
                    List.of(), 0, 0, 0, 25, true, true
            );

            when(notificationRepository
                    .findActiveAndNotExpiredByUserIdPaginated(
                            eq(USER_ID), eq(0), eq(25), any(LocalDateTime.class)
                    )
            ).thenReturn(expected);

            PaginatedNotifications result =
                    notificationService.getUserNotificationsPage(USER_ID, 0, 25);

            assertThat(result).isSameAs(expected);
        }

        @Test
        @DisplayName("Should reject negative page")
        void shouldRejectNegativePage() {
            assertThatThrownBy(() ->
                    notificationService.getUserNotificationsPage(USER_ID, -1, 25)
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject non-positive size")
        void shouldRejectNonPositiveSize() {
            assertThatThrownBy(() ->
                    notificationService.getUserNotificationsPage(USER_ID, 0, 0)
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject size exceeding max page size")
        void shouldRejectSizeExceedingMaxPageSize() {
            assertThatThrownBy(() ->
                    notificationService.getUserNotificationsPage(USER_ID, 0, 101)
            ).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("getUnreadCount")
    class GetUnreadCount {

        @Test
        @DisplayName("Should delegate to repository")
        void shouldDelegateToRepository() {
            when(notificationRepository
                    .countUnreadActiveAndNotExpiredByUserId(
                            eq(USER_ID), any(LocalDateTime.class)
                    )
            ).thenReturn(5L);

            long result = notificationService.getUnreadCount(USER_ID);

            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("findUserNotificationByTypeAndMetadataValue")
    class FindByTypeAndMetadata {

        @Test
        @DisplayName("Should return notification when found")
        void shouldReturnNotificationWhenFound() {
            Notification notification = activeNotification();

            when(notificationRepository
                    .findActiveByUserIdAndTypeAndMetadataValue(
                            USER_ID,
                            NotificationType.GOAL_INVITATION,
                            "goalId",
                            "some-goal-id"
                    )
            ).thenReturn(Optional.of(notification));

            Optional<Notification> result =
                    notificationService.findUserNotificationByTypeAndMetadataValue(
                            USER_ID,
                            NotificationType.GOAL_INVITATION,
                            "goalId",
                            "some-goal-id"
                    );

            assertThat(result).contains(notification);
        }

        @Test
        @DisplayName("Should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            when(notificationRepository
                    .findActiveByUserIdAndTypeAndMetadataValue(
                            USER_ID,
                            NotificationType.GOAL_INVITATION,
                            "goalId",
                            "non-existent"
                    )
            ).thenReturn(Optional.empty());

            Optional<Notification> result =
                    notificationService.findUserNotificationByTypeAndMetadataValue(
                            USER_ID,
                            NotificationType.GOAL_INVITATION,
                            "goalId",
                            "non-existent"
                    );

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("markAsRead")
    class MarkAsRead {

        @Test
        @DisplayName("Should save when notification is marked as read for the first time")
        void shouldSaveWhenMarkedAsReadForFirstTime() {
            Notification notification = activeNotification();
            UUID notificationId = notification.getId();

            when(notificationRepository
                    .findActiveAndNotExpiredByIdAndUserId(
                            eq(notificationId), eq(USER_ID), any(LocalDateTime.class)
                    )
            ).thenReturn(Optional.of(notification));

            notificationService.markAsRead(USER_ID, notificationId);

            assertThat(notification.isRead()).isTrue();
            verify(notificationRepository).save(notification);
        }

        @Test
        @DisplayName("Should not save when notification is already read")
        void shouldNotSaveWhenAlreadyRead() {
            Notification notification = Notification.reconstitute(
                    UUID.randomUUID(),
                    USER_ID,
                    NotificationType.SYSTEM,
                    "Title",
                    "Message",
                    null,
                    true,
                    false,
                    null,
                    LocalDateTime.now(),
                    null,
                    true
            );
            UUID notificationId = notification.getId();

            when(notificationRepository
                    .findActiveAndNotExpiredByIdAndUserId(
                            eq(notificationId), eq(USER_ID), any(LocalDateTime.class)
                    )
            ).thenReturn(Optional.of(notification));

            notificationService.markAsRead(USER_ID, notificationId);

            verify(notificationRepository, never()).save(any(Notification.class));
        }

        @Test
        @DisplayName("Should throw when notification is not found")
        void shouldThrowWhenNotificationNotFound() {
            UUID notificationId = UUID.randomUUID();

            when(notificationRepository
                    .findActiveAndNotExpiredByIdAndUserId(
                            eq(notificationId), eq(USER_ID), any(LocalDateTime.class)
                    )
            ).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    notificationService.markAsRead(USER_ID, notificationId)
            ).isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("markAsActioned")
    class MarkAsActioned {

        @Test
        @DisplayName("Should save and return true when actioned for the first time")
        void shouldSaveAndReturnTrueWhenActionedForFirstTime() {
            Notification notification = activeNotification();
            UUID notificationId = notification.getId();

            when(notificationRepository
                    .findActiveAndNotExpiredByIdAndUserId(
                            eq(notificationId), eq(USER_ID), any(LocalDateTime.class)
                    )
            ).thenReturn(Optional.of(notification));

            boolean result =
                    notificationService.markAsActioned(USER_ID, notificationId);

            assertThat(result).isTrue();
            assertThat(notification.getActionedAt()).isNotNull();
            verify(notificationRepository).save(notification);
        }

        @Test
        @DisplayName("Should return true without saving when already actioned")
        void shouldReturnTrueWithoutSavingWhenAlreadyActioned() {
            Notification notification = activeNotification();
            notification.markAsActioned();
            UUID notificationId = notification.getId();

            when(notificationRepository
                    .findActiveAndNotExpiredByIdAndUserId(
                            eq(notificationId), eq(USER_ID), any(LocalDateTime.class)
                    )
            ).thenReturn(Optional.of(notification));

            boolean result =
                    notificationService.markAsActioned(USER_ID, notificationId);

            assertThat(result).isTrue();
            verify(notificationRepository, never()).save(any(Notification.class));
        }

        @Test
        @DisplayName("Should return false when notification is not found")
        void shouldReturnFalseWhenNotificationNotFound() {
            UUID notificationId = UUID.randomUUID();

            when(notificationRepository
                    .findActiveAndNotExpiredByIdAndUserId(
                            eq(notificationId), eq(USER_ID), any(LocalDateTime.class)
                    )
            ).thenReturn(Optional.empty());

            boolean result =
                    notificationService.markAsActioned(USER_ID, notificationId);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should propagate exception when notification does not require action")
        void shouldPropagateExceptionWhenNotificationDoesNotRequireAction() {
            Notification notification = Notification.create(
                    USER_ID,
                    NotificationType.SYSTEM,
                    "Title",
                    "Message",
                    null,
                    false,
                    null
            );
            UUID notificationId = notification.getId();

            when(notificationRepository
                    .findActiveAndNotExpiredByIdAndUserId(
                            eq(notificationId), eq(USER_ID), any(LocalDateTime.class)
                    )
            ).thenReturn(Optional.of(notification));

            assertThatThrownBy(() ->
                    notificationService.markAsActioned(USER_ID, notificationId)
            ).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Should soft delete and save notification")
        void shouldSoftDeleteAndSaveNotification() {
            Notification notification = activeNotification();
            UUID notificationId = notification.getId();

            when(notificationRepository
                    .findActiveAndNotExpiredByIdAndUserId(
                            eq(notificationId), eq(USER_ID), any(LocalDateTime.class)
                    )
            ).thenReturn(Optional.of(notification));

            notificationService.delete(USER_ID, notificationId);

            assertThat(notification.isActive()).isFalse();
            verify(notificationRepository).save(notification);
        }

        @Test
        @DisplayName("Should throw when notification is not found")
        void shouldThrowWhenNotificationNotFound() {
            UUID notificationId = UUID.randomUUID();

            when(notificationRepository
                    .findActiveAndNotExpiredByIdAndUserId(
                            eq(notificationId), eq(USER_ID), any(LocalDateTime.class)
                    )
            ).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    notificationService.delete(USER_ID, notificationId)
            ).isInstanceOf(ResourceNotFoundException.class);
        }
    }
}