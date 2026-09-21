package com.puntomartinez.millete.groupgoals.domain.model;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GoalInvitation aggregate")
class GoalInvitationTest {

    private static final UUID GOAL_ID = UUID.randomUUID();
    private static final UUID INVITER_ID = UUID.randomUUID();
    private static final UUID INVITED_ID = UUID.randomUUID();

    private GoalInvitation createValid() {
        return GoalInvitation.create(GOAL_ID, INVITER_ID, INVITED_ID);
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Should create valid pending invitation")
        void shouldCreateValidPendingInvitation() {
            GoalInvitation invitation = createValid();

            assertThat(invitation.getId()).isNotNull();
            assertThat(invitation.getGoalId()).isEqualTo(GOAL_ID);
            assertThat(invitation.getInviterUserId()).isEqualTo(INVITER_ID);
            assertThat(invitation.getInvitedUserId()).isEqualTo(INVITED_ID);
            assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.PENDING);
            assertThat(invitation.isActive()).isTrue();
            assertThat(invitation.getExpiresAt()).isNotNull();
            assertThat(invitation.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should set expiration to 7 days")
        void shouldSetExpirationTo7Days() {
            GoalInvitation invitation = createValid();

            LocalDateTime expectedExpiry =
                    invitation.getCreatedAt().plusDays(7);

            assertThat(invitation.getExpiresAt()).isEqualTo(expectedExpiry);
        }

        @Test
        @DisplayName("Should reject null goal id")
        void shouldRejectNullGoalId() {
            assertThatThrownBy(() -> GoalInvitation.create(
                    null, INVITER_ID, INVITED_ID
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null inviter user id")
        void shouldRejectNullInviterUserId() {
            assertThatThrownBy(() -> GoalInvitation.create(
                    GOAL_ID, null, INVITED_ID
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null invited user id")
        void shouldRejectNullInvitedUserId() {
            assertThatThrownBy(() -> GoalInvitation.create(
                    GOAL_ID, INVITER_ID, null
            )).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("reconstitute")
    class Reconstitute {

        @Test
        @DisplayName("Should reconstitute existing invitation")
        void shouldReconstituteInvitation() {
            UUID id = UUID.randomUUID();
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
            LocalDateTime createdAt = LocalDateTime.now();
            LocalDateTime modifiedAt = LocalDateTime.now();

            GoalInvitation invitation = GoalInvitation.reconstitute(
                    id, GOAL_ID, INVITER_ID, INVITED_ID,
                    InvitationStatus.PENDING, expiresAt,
                    createdAt, modifiedAt, true
            );

            assertThat(invitation.getId()).isEqualTo(id);
            assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.PENDING);
        }

        @Test
        @DisplayName("Should reject null id on reconstitute")
        void shouldRejectNullIdOnReconstitute() {
            assertThatThrownBy(() -> GoalInvitation.reconstitute(
                    null, GOAL_ID, INVITER_ID, INVITED_ID,
                    InvitationStatus.PENDING, LocalDateTime.now().plusDays(7),
                    LocalDateTime.now(), LocalDateTime.now(), true
            )).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("isAcceptable")
    class IsAcceptable {

        @Test
        @DisplayName("Should return true for pending active not expired invitation")
        void shouldReturnTrueForPendingActiveNotExpired() {
            GoalInvitation invitation = createValid();

            assertThat(invitation.isAcceptable()).isTrue();
        }

        @Test
        @DisplayName("Should return false for accepted invitation")
        void shouldReturnFalseForAcceptedInvitation() {
            GoalInvitation invitation = createValid();
            invitation.markAsAccepted();

            assertThat(invitation.isAcceptable()).isFalse();
        }

        @Test
        @DisplayName("Should return false for expired invitation")
        void shouldReturnFalseForExpiredInvitation() {
            GoalInvitation invitation = GoalInvitation.reconstitute(
                    UUID.randomUUID(), GOAL_ID, INVITER_ID, INVITED_ID,
                    InvitationStatus.PENDING, LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().minusDays(8), LocalDateTime.now(), true
            );

            assertThat(invitation.isAcceptable()).isFalse();
        }

        @Test
        @DisplayName("Should return false for inactive invitation")
        void shouldReturnFalseForInactiveInvitation() {
            GoalInvitation invitation = createValid();
            invitation.deactivate();

            assertThat(invitation.isAcceptable()).isFalse();
        }
    }

    @Nested
    @DisplayName("markAsAccepted and markAsRejected")
    class MarkStatus {

        @Test
        @DisplayName("Should mark as accepted")
        void shouldMarkAsAccepted() {
            GoalInvitation invitation = createValid();

            invitation.markAsAccepted();

            assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
            assertThat(invitation.getModifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should mark as rejected")
        void shouldMarkAsRejected() {
            GoalInvitation invitation = createValid();

            invitation.markAsRejected();

            assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.REJECTED);
            assertThat(invitation.getModifiedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("isExpired")
    class IsExpired {

        @Test
        @DisplayName("Should return false when not expired")
        void shouldReturnFalseWhenNotExpired() {
            GoalInvitation invitation = createValid();

            assertThat(invitation.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Should return true when expired")
        void shouldReturnTrueWhenExpired() {
            GoalInvitation invitation = GoalInvitation.reconstitute(
                    UUID.randomUUID(), GOAL_ID, INVITER_ID, INVITED_ID,
                    InvitationStatus.PENDING, LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().minusDays(8), LocalDateTime.now(), true
            );

            assertThat(invitation.isExpired()).isTrue();
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("Should deactivate invitation")
        void shouldDeactivateInvitation() {
            GoalInvitation invitation = createValid();

            invitation.deactivate();

            assertThat(invitation.isActive()).isFalse();
            assertThat(invitation.getModifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should not change modified at when already inactive")
        void shouldNotChangeModifiedAtWhenAlreadyInactive() {
            GoalInvitation invitation = createValid();
            invitation.deactivate();
            LocalDateTime previousModifiedAt = invitation.getModifiedAt();

            invitation.deactivate();

            assertThat(invitation.getModifiedAt()).isEqualTo(previousModifiedAt);
        }
    }
}