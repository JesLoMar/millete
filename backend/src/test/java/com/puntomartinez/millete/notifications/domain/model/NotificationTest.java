package com.puntomartinez.millete.notifications.domain.model;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Notification aggregate")
class NotificationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String VALID_TITLE = "Goal invitation";
    private static final String VALID_MESSAGE = "You have been invited to a goal.";

    private Notification createValid() {
        return Notification.create(
                USER_ID,
                NotificationType.GOAL_INVITATION,
                VALID_TITLE,
                VALID_MESSAGE,
                Map.of("goalId", UUID.randomUUID().toString()),
                true,
                LocalDateTime.now().plusDays(7)
        );
    }

    private Notification reconstituteValid(boolean active) {
        return Notification.reconstitute(
                UUID.randomUUID(),
                USER_ID,
                NotificationType.GOAL_INVITATION,
                VALID_TITLE,
                VALID_MESSAGE,
                Map.of("goalId", UUID.randomUUID().toString()),
                false,
                true,
                null,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                LocalDateTime.now().plusDays(7),
                active
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Should create a valid active unread notification")
        void shouldCreateValidNotification() {
            Notification notification = createValid();

            assertThat(notification.getId()).isNotNull();
            assertThat(notification.getUserId()).isEqualTo(USER_ID);
            assertThat(notification.getType()).isEqualTo(NotificationType.GOAL_INVITATION);
            assertThat(notification.getTitle()).isEqualTo(VALID_TITLE);
            assertThat(notification.getMessage()).isEqualTo(VALID_MESSAGE);
            assertThat(notification.getMetadata()).containsKey("goalId");
            assertThat(notification.isRead()).isFalse();
            assertThat(notification.isActionRequired()).isTrue();
            assertThat(notification.getActionedAt()).isNull();
            assertThat(notification.getCreatedAt()).isNotNull();
            assertThat(notification.getExpiresAt()).isNotNull();
            assertThat(notification.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should allow null expiresAt")
        void shouldAllowNullExpiresAt() {
            Notification notification = Notification.create(
                    USER_ID,
                    NotificationType.SYSTEM,
                    VALID_TITLE,
                    VALID_MESSAGE,
                    null,
                    false,
                    null
            );

            assertThat(notification.getExpiresAt()).isNull();
        }

        @Test
        @DisplayName("Should allow null metadata")
        void shouldAllowNullMetadata() {
            Notification notification = Notification.create(
                    USER_ID,
                    NotificationType.SYSTEM,
                    VALID_TITLE,
                    VALID_MESSAGE,
                    null,
                    false,
                    null
            );

            assertThat(notification.getMetadata()).isNull();
        }

        @Test
        @DisplayName("Should reject null userId")
        void shouldRejectNullUserId() {
            assertThatThrownBy(() ->
                    Notification.create(
                            null,
                            NotificationType.SYSTEM,
                            VALID_TITLE,
                            VALID_MESSAGE,
                            null,
                            false,
                            null
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null type")
        void shouldRejectNullType() {
            assertThatThrownBy(() ->
                    Notification.create(
                            USER_ID,
                            null,
                            VALID_TITLE,
                            VALID_MESSAGE,
                            null,
                            false,
                            null
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Should reject blank title")
        void shouldRejectBlankTitle(String title) {
            assertThatThrownBy(() ->
                    Notification.create(
                            USER_ID,
                            NotificationType.SYSTEM,
                            title,
                            VALID_MESSAGE,
                            null,
                            false,
                            null
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject title exceeding max length")
        void shouldRejectTitleExceedingMaxLength() {
            String longTitle = "A".repeat(256);

            assertThatThrownBy(() ->
                    Notification.create(
                            USER_ID,
                            NotificationType.SYSTEM,
                            longTitle,
                            VALID_MESSAGE,
                            null,
                            false,
                            null
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should allow title at max length")
        void shouldAllowTitleAtMaxLength() {
            String maxTitle = "A".repeat(255);

            Notification notification = Notification.create(
                    USER_ID,
                    NotificationType.SYSTEM,
                    maxTitle,
                    VALID_MESSAGE,
                    null,
                    false,
                    null
            );

            assertThat(notification.getTitle()).isEqualTo(maxTitle);
        }

        @Test
        @DisplayName("Should protect metadata from external mutation")
        void shouldProtectMetadataFromExternalMutation() {
            Map<String, Object> mutableMetadata = new HashMap<>();
            mutableMetadata.put("key", "value");

            Notification notification = Notification.create(
                    USER_ID,
                    NotificationType.SYSTEM,
                    VALID_TITLE,
                    VALID_MESSAGE,
                    mutableMetadata,
                    false,
                    null
            );

            mutableMetadata.put("hacked", "value");

            assertThat(notification.getMetadata()).doesNotContainKey("hacked");
        }
    }

    @Nested
    @DisplayName("reconstitute")
    class Reconstitute {

        @Test
        @DisplayName("Should reconstitute an existing notification")
        void shouldReconstituteNotification() {
            UUID id = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime expiresAt = LocalDateTime.of(2024, 12, 31, 23, 59);
            LocalDateTime actionedAt = LocalDateTime.of(2024, 1, 2, 10, 0);

            Notification notification = Notification.reconstitute(
                    id,
                    USER_ID,
                    NotificationType.SYSTEM,
                    "System alert",
                    "Something happened",
                    Map.of("key", "value"),
                    true,
                    true,
                    actionedAt,
                    createdAt,
                    expiresAt,
                    false
            );

            assertThat(notification.getId()).isEqualTo(id);
            assertThat(notification.getUserId()).isEqualTo(USER_ID);
            assertThat(notification.getType()).isEqualTo(NotificationType.SYSTEM);
            assertThat(notification.getTitle()).isEqualTo("System alert");
            assertThat(notification.getMessage()).isEqualTo("Something happened");
            assertThat(notification.getMetadata()).containsEntry("key", "value");
            assertThat(notification.isRead()).isTrue();
            assertThat(notification.isActionRequired()).isTrue();
            assertThat(notification.getActionedAt()).isEqualTo(actionedAt);
            assertThat(notification.getCreatedAt()).isEqualTo(createdAt);
            assertThat(notification.getExpiresAt()).isEqualTo(expiresAt);
            assertThat(notification.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should reject null id on reconstitute")
        void shouldRejectNullIdOnReconstitute() {
            assertThatThrownBy(() ->
                    Notification.reconstitute(
                            null,
                            USER_ID,
                            NotificationType.SYSTEM,
                            VALID_TITLE,
                            VALID_MESSAGE,
                            null,
                            false,
                            false,
                            null,
                            LocalDateTime.now(),
                            null,
                            true
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null createdAt on reconstitute")
        void shouldRejectNullCreatedAtOnReconstitute() {
            assertThatThrownBy(() ->
                    Notification.reconstitute(
                            UUID.randomUUID(),
                            USER_ID,
                            NotificationType.SYSTEM,
                            VALID_TITLE,
                            VALID_MESSAGE,
                            null,
                            false,
                            false,
                            null,
                            null,
                            null,
                            true
                    )
            ).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("markAsRead")
    class MarkAsRead {

        @Test
        @DisplayName("Should mark as read and return true when unread")
        void shouldMarkAsReadWhenUnread() {
            Notification notification = createValid();

            boolean changed = notification.markAsRead();

            assertThat(changed).isTrue();
            assertThat(notification.isRead()).isTrue();
        }

        @Test
        @DisplayName("Should return false when already read")
        void shouldReturnFalseWhenAlreadyRead() {
            Notification notification = reconstituteValid(true);
            notification.markAsRead();

            boolean changed = notification.markAsRead();

            assertThat(changed).isFalse();
            assertThat(notification.isRead()).isTrue();
        }
    }

    @Nested
    @DisplayName("markAsActioned")
    class MarkAsActioned {

        @Test
        @DisplayName("Should mark as actioned and return true when not yet actioned")
        void shouldMarkAsActionedWhenNotYetActioned() {
            Notification notification = createValid();

            boolean changed = notification.markAsActioned();

            assertThat(changed).isTrue();
            assertThat(notification.getActionedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should return false when already actioned")
        void shouldReturnFalseWhenAlreadyActioned() {
            Notification notification = createValid();
            notification.markAsActioned();

            boolean changed = notification.markAsActioned();

            assertThat(changed).isFalse();
        }

        @Test
        @DisplayName("Should throw when notification does not require action")
        void shouldThrowWhenNotificationDoesNotRequireAction() {
            Notification notification = Notification.create(
                    USER_ID,
                    NotificationType.SYSTEM,
                    VALID_TITLE,
                    VALID_MESSAGE,
                    null,
                    false,
                    null
            );

            assertThatThrownBy(notification::markAsActioned)
                    .isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("softDelete")
    class SoftDelete {

        @Test
        @DisplayName("Should set active to false")
        void shouldSetActiveToFalse() {
            Notification notification = createValid();

            notification.softDelete();

            assertThat(notification.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("isExpired")
    class IsExpired {

        @Test
        @DisplayName("Should return false when expiresAt is null")
        void shouldReturnFalseWhenExpiresAtIsNull() {
            Notification notification = Notification.create(
                    USER_ID,
                    NotificationType.SYSTEM,
                    VALID_TITLE,
                    VALID_MESSAGE,
                    null,
                    false,
                    null
            );

            assertThat(notification.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Should return false when expiresAt is in the future")
        void shouldReturnFalseWhenExpiresAtIsInFuture() {
            Notification notification = Notification.create(
                    USER_ID,
                    NotificationType.SYSTEM,
                    VALID_TITLE,
                    VALID_MESSAGE,
                    null,
                    false,
                    LocalDateTime.now().plusDays(1)
            );

            assertThat(notification.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Should return true when expiresAt is in the past")
        void shouldReturnTrueWhenExpiresAtIsInPast() {
            Notification notification = Notification.reconstitute(
                    UUID.randomUUID(),
                    USER_ID,
                    NotificationType.SYSTEM,
                    VALID_TITLE,
                    VALID_MESSAGE,
                    null,
                    false,
                    false,
                    null,
                    LocalDateTime.now().minusDays(2),
                    LocalDateTime.now().minusDays(1),
                    true
            );

            assertThat(notification.isExpired()).isTrue();
        }
    }
}