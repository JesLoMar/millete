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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController")
class NotificationControllerTest {

    private static final UUID USER_ID = UUID.randomUUID();

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
    @DisplayName("getNotifications")
    class GetNotifications {

        @Test
        @DisplayName("Should pass provided limit to use case")
        void shouldPassProvidedLimitToUseCase() {
            mockAuthenticatedUser();
            Notification notification = activeNotification();

            when(getNotificationsUseCase.getUserNotifications(USER_ID, 10))
                    .thenReturn(List.of(notification));

            ResponseEntity<List<NotificationResponseDTO>> response =
                    controller.getNotifications(authentication, 10);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().getFirst().title())
                    .isEqualTo("Goal invitation");
            verify(getNotificationsUseCase).getUserNotifications(USER_ID, 10);
        }

        @Test
        @DisplayName("Should use Integer.MAX_VALUE when limit is null")
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
        @DisplayName("Should delegate and map to PaginatedResponseDTO")
        void shouldDelegateAndMapToPaginatedResponse() {
            mockAuthenticatedUser();
            Notification notification = activeNotification();

            PaginatedNotifications paginatedResult = new PaginatedNotifications(
                    List.of(notification), 0, 1, 1, 25, true, true
            );

            when(getNotificationsUseCase.getUserNotificationsPage(USER_ID, 0, 25))
                    .thenReturn(paginatedResult);

            ResponseEntity<PaginatedResponseDTO<NotificationResponseDTO>> response =
                    controller.getNotificationsPaginated(authentication, 0, 25);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            PaginatedResponseDTO<NotificationResponseDTO> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.content()).hasSize(1);
            assertThat(body.currentPage()).isZero();
            assertThat(body.totalPages()).isEqualTo(1);
            assertThat(body.totalElements()).isEqualTo(1L);
            assertThat(body.first()).isTrue();
            assertThat(body.last()).isTrue();
        }
    }

    @Nested
    @DisplayName("getUnreadCount")
    class GetUnreadCount {

        @Test
        @DisplayName("Should return count in map")
        void shouldReturnCountInMap() {
            mockAuthenticatedUser();

            when(getNotificationsUseCase.getUnreadCount(USER_ID)).thenReturn(5L);

            ResponseEntity<Map<String, Long>> response =
                    controller.getUnreadCount(authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("count", 5L);
        }
    }

    @Nested
    @DisplayName("markAsRead")
    class MarkAsRead {

        @Test
        @DisplayName("Should delegate and return 200")
        void shouldDelegateAndReturn200() {
            mockAuthenticatedUser();
            UUID notificationId = UUID.randomUUID();

            ResponseEntity<Void> response =
                    controller.markAsRead(authentication, notificationId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(markNotificationAsReadUseCase)
                    .markAsRead(USER_ID, notificationId);
        }
    }

    @Nested
    @DisplayName("markAsActioned")
    class MarkAsActioned {

        @Test
        @DisplayName("Should return 200 when actioned is true")
        void shouldReturn200WhenActionedIsTrue() {
            mockAuthenticatedUser();
            UUID notificationId = UUID.randomUUID();

            when(markNotificationAsActionedUseCase
                    .markAsActioned(USER_ID, notificationId)
            ).thenReturn(true);

            ResponseEntity<Void> response =
                    controller.markAsActioned(authentication, notificationId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Should return 404 when actioned is false")
        void shouldReturn404WhenActionedIsFalse() {
            mockAuthenticatedUser();
            UUID notificationId = UUID.randomUUID();

            when(markNotificationAsActionedUseCase
                    .markAsActioned(USER_ID, notificationId)
            ).thenReturn(false);

            ResponseEntity<Void> response =
                    controller.markAsActioned(authentication, notificationId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Should delegate and return 204")
        void shouldDelegateAndReturn204() {
            mockAuthenticatedUser();
            UUID notificationId = UUID.randomUUID();

            ResponseEntity<Void> response =
                    controller.delete(authentication, notificationId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(deleteNotificationUseCase).delete(USER_ID, notificationId);
        }
    }
}