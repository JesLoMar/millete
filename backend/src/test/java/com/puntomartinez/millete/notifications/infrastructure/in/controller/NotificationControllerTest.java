package com.puntomartinez.millete.notifications.infrastructure.in.controller;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.domain.model.PaginatedNotifications;
import com.puntomartinez.millete.notifications.domain.ports.in.DeleteNotificationUseCase;
import com.puntomartinez.millete.notifications.domain.ports.in.GetNotificationsUseCase;
import com.puntomartinez.millete.notifications.domain.ports.in.MarkNotificationAsActionedUseCase;
import com.puntomartinez.millete.notifications.domain.ports.in.MarkNotificationAsReadUseCase;
import com.puntomartinez.millete.notifications.infrastructure.in.controller.dto.NotificationResponseDTO;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import com.puntomartinez.millete.shared.domain.time.FixedTimeProvider;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController")

class NotificationControllerTest {
    static final TimeProvider TIME = new FixedTimeProvider(
            Instant.parse("2024-01-15T10:00:00Z"));


    private static final TimeProvider TIME =
            new FixedTimeProvider(Instant.parse("2024-01-01T10:00:00Z"));


    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID NOTIFICATION_ID = UUID.randomUUID();

    @Mock
    private GetNotificationsUseCase getNotificationsUseCase;

    @Mock
    private MarkNotificationAsReadUseCase markNotificationAsReadUseCase;

    @Mock
    private MarkNotificationAsActionedUseCase markNotificationAsActionedUseCase;

    @Mock
    private DeleteNotificationUseCase deleteNotificationUseCase;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NotificationController controller;

    private void mockAuthenticatedUser() {
        JwtUser jwtUser = new JwtUser(USER_ID, "username", "user@example.com");
        when(authentication.getPrincipal()).thenReturn(jwtUser);
    }

    private Notification validNotification() {
        return Notification.create(TIME, 
                USER_ID,
                NotificationType.GOAL_INVITATION,
                "New invitation",
                "You have been invited",
                Map.of("goalId", UUID.randomUUID().toString()),
                true,
                Instant.now().plusDays(7)
        );
    }

    @Nested
    @DisplayName("getNotifications")
    class GetNotifications {

        @Test
        @DisplayName("Should return notifications with provided limit")
        void shouldReturnNotificationsWithLimit() {
            mockAuthenticatedUser();

            when(getNotificationsUseCase.getUserNotifications(USER_ID, 10))
                    .thenReturn(List.of(validNotification()));

            ResponseEntity<List<NotificationResponseDTO>> response =
                    controller.getNotifications(authentication, 10);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().getFirst().title())
                    .isEqualTo("New invitation");
        }

        @Test
        @DisplayName("Should use MAX_VALUE when limit is null")
        void shouldUseMaxValueWhenLimitIsNull() {
            mockAuthenticatedUser();

            when(getNotificationsUseCase.getUserNotifications(
                    eq(USER_ID), eq(Integer.MAX_VALUE)
            )).thenReturn(List.of());

            ResponseEntity<List<NotificationResponseDTO>> response =
                    controller.getNotifications(authentication, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getNotificationsUseCase)
                    .getUserNotifications(USER_ID, Integer.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("getNotificationsPaginated")
    class GetNotificationsPaginated {

        @Test
        @DisplayName("Should return paginated response")
        void shouldReturnPaginatedResponse() {
            mockAuthenticatedUser();

            PaginatedNotifications paginated = new PaginatedNotifications(
                    List.of(validNotification()), 0, 1, 1, 25, true, true
            );

            when(getNotificationsUseCase.getUserNotificationsPage(USER_ID, 0, 25))
                    .thenReturn(paginated);

            ResponseEntity<PaginatedResponseDTO<NotificationResponseDTO>> response =
                    controller.getNotificationsPaginated(authentication, 0, 25);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().content()).hasSize(1);
            assertThat(response.getBody().currentPage()).isZero();
        }
    }

    @Nested
    @DisplayName("getUnreadCount")
    class GetUnreadCount {

        @Test
        @DisplayName("Should return unread count in map")
        void shouldReturnUnreadCount() {
            mockAuthenticatedUser();

            when(getNotificationsUseCase.getUnreadCount(USER_ID)).thenReturn(3L);

            ResponseEntity<Map<String, Long>> response =
                    controller.getUnreadCount(authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("count", 3L);
        }
    }

    @Nested
    @DisplayName("markAsRead")
    class MarkAsRead {

        @Test
        @DisplayName("Should mark as read and return 200")
        void shouldMarkAsReadAndReturn200() {
            mockAuthenticatedUser();

            ResponseEntity<Void> response =
                    controller.markAsRead(authentication, NOTIFICATION_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(markNotificationAsReadUseCase)
                    .markAsRead(USER_ID, NOTIFICATION_ID);
        }
    }

    @Nested
    @DisplayName("markAsActioned")
    class MarkAsActioned {

        @Test
        @DisplayName("Should return 200 when actioned successfully")
        void shouldReturn200WhenActioned() {
            mockAuthenticatedUser();

            when(markNotificationAsActionedUseCase.markAsActioned(
                    USER_ID, NOTIFICATION_ID
            )).thenReturn(true);

            ResponseEntity<Void> response =
                    controller.markAsActioned(authentication, NOTIFICATION_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Should return 404 when notification not found")
        void shouldReturn404WhenNotFound() {
            mockAuthenticatedUser();

            when(markNotificationAsActionedUseCase.markAsActioned(
                    USER_ID, NOTIFICATION_ID
            )).thenReturn(false);

            ResponseEntity<Void> response =
                    controller.markAsActioned(authentication, NOTIFICATION_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Should delete and return 204")
        void shouldDeleteAndReturn204() {
            mockAuthenticatedUser();

            ResponseEntity<Void> response =
                    controller.delete(authentication, NOTIFICATION_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(deleteNotificationUseCase).delete(USER_ID, NOTIFICATION_ID);
        }
    }
}