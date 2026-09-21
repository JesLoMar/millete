package com.puntomartinez.millete.notifications.domain.model;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Notification aggregate")
class NotificationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String VALID_TITLE = "New invitation";
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
        @DisplayName("Should create valid notification with all fields")
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
        @DisplayName("Should create notification with null metadata")
        void shouldCreateWithNullMetadata() {
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
            assertThat(notification.isActionRequired()).isFalse();
            assertThat(notification.getExpiresAt()).isNull();
        }

        @Test
        @DisplayName("Should create notification with null expiresAt")
        void shouldCreateWithNullExpiresAt() {
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
        @DisplayName("Should reject null userId")
        void shouldRejectNullUserId() {
            assertThatThrownBy(() -> Notification.create(
                    null,
                    NotificationType.SYSTEM,
                    VALID_TITLE,
                    VALID_MESSAGE,
                    null,
                    false,
                    null
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null type")
        void shouldRejectNullType() {
            assertThatThrownBy(() -> Notification.create(
                    USER_ID,
                    null,
                    VALID_TITLE,
                    VALID_MESSAGE,
                    null,
                    false,
                    null
            )).isInstanceOf(InvalidInputException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Should reject blank title")
        void shouldRejectBlankTitle(String title) {
            assertThatThrownBy(() -> Notification.create(
                    USER_ID,
                    NotificationType.SYSTEM,
                    title,
                    VALID_MESSAGE,
                    null,
                    false,
                    null
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject title exceeding 255 characters")
        void shouldRejectTitleExceedingMaxLength() {
            String longTitle = "A".repeat(256);

            assertThatThrownBy(() -> Notification.create(
                    USER_ID,
                    NotificationType.SYSTEM,
                    longTitle,
                    VALID_MESSAGE,
                    null,
                    false,
                    null
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should allow title at exactly 255 characters")
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

            assertThat(notification.getTitle()).hasSize(255);
        }

        @Test
        @DisplayName("Should make metadata immutable via defensive copy")
        void shouldMakeMetadataImmutable() {
            var mutableMetadata = new java.util.HashMap<String, Object>();
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

            mutableMetadata.put("newKey", "newValue");

            assertThat(notification.getMetadata()).doesNotContainKey("newKey");
        }
    }

    @Nested
    @DisplayName("reconstitute")
    class Reconstitute {

        @Test
        @DisplayName("Should reconstitute existing notification")
        void shouldReconstituteNotification() {
            UUID id = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

            Notification notification = Notification.reconstitute(
                    id, USER_ID, NotificationType.SYSTEM,
                    VALID_TITLE, VALID_MESSAGE, null,
                    true, false, null, createdAt, expiresAt, true
            );

            assertThat(notification.getId()).isEqualTo(id);
            assertThat(notification.isRead()).isTrue();
            assertThat(notification.isActionRequired()).isFalse();
            assertThat(notification.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should reject null id on reconstitute")
        void shouldRejectNullIdOnReconstitute() {
            assertThatThrownBy(() -> Notification.reconstitute(
                    null, USER_ID, NotificationType.SYSTEM,
                    VALID_TITLE, VALID_MESSAGE, null,
                    false, false, null,
                    LocalDateTime.now(), null, true
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null createdAt on reconstitute")
        void shouldRejectNullCreatedAtOnReconstitute() {
            assertThatThrownBy(() -> Notification.reconstitute(
                    UUID.randomUUID(), USER_ID, NotificationType.SYSTEM,
                    VALID_TITLE, VALID_MESSAGE, null,
                    false, false, null,
                    null, null, true
            )).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("markAsRead")
    class MarkAsRead {

        @Test
        @DisplayName("Should mark unread notification as read and return true")
        void shouldMarkUnreadAsRead() {
            Notification notification = createValid();

            boolean changed = notification.markAsRead();

            assertThat(changed).isTrue();
            assertThat(notification.isRead()).isTrue();
        }

        @Test
        @DisplayName("Should return false when already read")
        void shouldReturnFalseWhenAlreadyRead() {
            Notification notification = createValid();
            notification.markAsRead();

            boolean changed = notification.markAsRead();

            assertThat(changed).isFalse();
        }
    }

    @Nested
    @DisplayName("markAsActioned")
    class MarkAsActioned {

        @Test
        @DisplayName("Should mark action-required notification as actioned")
        void shouldMarkAsActioned() {
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
        void shouldThrowWhenNotActionRequired() {
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
        @DisplayName("Should deactivate notification")
        void shouldDeactivateNotification() {
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
        void shouldReturnFalseWhenNoExpiry() {
            Notification notification = Notification.create(
                    USER_ID, NotificationType.SYSTEM,
                    VALID_TITLE, VALID_MESSAGE, null, false, null
            );

            assertThat(notification.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Should return false when not yet expired")
        void shouldReturnFalseWhenNotExpired() {
            Notification notification = createValid();

            assertThat(notification.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Should return true when expired")
        void shouldReturnTrueWhenExpired() {
            Notification notification = Notification.reconstitute(
                    UUID.randomUUID(), USER_ID, NotificationType.SYSTEM,
                    VALID_TITLE, VALID_MESSAGE, null,
                    false, false, null,
                    LocalDateTime.now().minusDays(10),
                    LocalDateTime.now().minusDays(1),
                    true
            );

            assertThat(notification.isExpired()).isTrue();
        }
    }
}