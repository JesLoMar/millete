package com.puntomartinez.millete.groupgoals.application.services;

import com.puntomartinez.millete.groupgoals.domain.model.*;
import com.puntomartinez.millete.groupgoals.domain.ports.in.InviteMemberUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.out.*;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceAlreadyExistsException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import com.puntomartinez.millete.shared.domain.time.FixedTimeProvider;
import org.mockito.Spy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupGoalInvitationService")
class GroupGoalInvitationServiceTest {
    static final TimeProvider TIME = new FixedTimeProvider(
            Instant.parse("2024-01-15T10:00:00Z"));


    @Mock
    private GoalInvitationRepository goalInvitationRepository;

    @Mock
    private GoalMemberRepository goalMemberRepository;

    @Mock
    private GoalUnitRepository goalUnitRepository;

    @Mock
    private UserLookupPort userLookupPort;

    @Mock
    private GoalInvitationNotificationPort notificationPort;

    private final TimeProvider TIME =
            new FixedTimeProvider(Instant.parse("2024-01-01T10:00:00Z"));

    private GroupGoalInvitationService service;

    private final UUID goalId = UUID.randomUUID();
    private final UUID inviterId = UUID.randomUUID();
    private final UUID invitedId = UUID.randomUUID();

    private GoalUnit createActiveGoal() {
        return GoalUnit.reconstitute(
                goalId, "Family trip", new BigDecimal("300.00"),
                DistributionMode.EQUITATIVE,
                Instant.now(), Instant.now(), true
        );
    }

    private GoalMember createAdminMember() {
        return GoalMember.reconstitute(
                UUID.randomUUID(), goalId, inviterId,
                GoalRole.ADMIN, null, null,
                Instant.now(), Instant.now(),
                Instant.now(), true
        );
    }


    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new GroupGoalInvitationService(
                goalInvitationRepository, goalMemberRepository, goalUnitRepository, userLookupPort, notificationPort, TIME
        );
    }

    @Nested
    @DisplayName("inviteMember")
    class InviteMember {

        @Test
        @DisplayName("Should create invitation for valid request")
        void shouldCreateInvitationForValidRequest() {
            GoalUnit goal = createActiveGoal();
            GoalMember admin = createAdminMember();
            UserLookupPort.UserInfo invitedUser =
                    new UserLookupPort.UserInfo(invitedId, "invited", "invited@mail.com");
            UserLookupPort.UserInfo inviterUser =
                    new UserLookupPort.UserInfo(inviterId, "inviter", "inviter@mail.com");

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, inviterId))
                    .thenReturn(Optional.of(admin));
            when(userLookupPort.findByIdentifier("invited@mail.com"))
                    .thenReturn(Optional.of(invitedUser));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, invitedId))
                    .thenReturn(Optional.empty());
            when(goalInvitationRepository.findByGoalIdAndInvitedUserIdAndStatus(
                    goalId, invitedId, InvitationStatus.PENDING
            )).thenReturn(Optional.empty());
            when(goalInvitationRepository.save(any(GoalInvitation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(userLookupPort.findById(inviterId))
                    .thenReturn(Optional.of(inviterUser));

            InviteMemberUseCase.InviteMemberCommand command =
                    new InviteMemberUseCase.InviteMemberCommand("invited@mail.com");

            InviteMemberUseCase.InvitationResult result =
                    service.inviteMember(goalId, inviterId, command);

            assertThat(result.goalId()).isEqualTo(goalId);
            assertThat(result.goalName()).isEqualTo("Family trip");
            assertThat(result.invitedUserId()).isEqualTo(invitedId);
            assertThat(result.status()).isEqualTo(InvitationStatus.PENDING);

            verify(goalInvitationRepository).save(any(GoalInvitation.class));
            verify(notificationPort).createInvitationNotification(
                    any(GoalInvitation.class),
                    eq("Family trip"),
                    eq("inviter")
            );
        }

        @Test
        @DisplayName("Should throw when inviting yourself")
        void shouldThrowWhenInvitingYourself() {
            GoalUnit goal = createActiveGoal();
            GoalMember admin = createAdminMember();
            UserLookupPort.UserInfo invitedUser =
                    new UserLookupPort.UserInfo(inviterId, "inviter", "inviter@mail.com");

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, inviterId))
                    .thenReturn(Optional.of(admin));
            when(userLookupPort.findByIdentifier("inviter@mail.com"))
                    .thenReturn(Optional.of(invitedUser));

            InviteMemberUseCase.InviteMemberCommand command =
                    new InviteMemberUseCase.InviteMemberCommand("inviter@mail.com");

            assertThatThrownBy(() ->
                    service.inviteMember(goalId, inviterId, command)
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should throw when user is already a member")
        void shouldThrowWhenUserIsAlreadyMember() {
            GoalUnit goal = createActiveGoal();
            GoalMember admin = createAdminMember();
            GoalMember existingMember = GoalMember.reconstitute(
                    UUID.randomUUID(), goalId, invitedId,
                    GoalRole.MEMBER, null, null,
                    Instant.now(), Instant.now(),
                    Instant.now(), true
            );
            UserLookupPort.UserInfo invitedUser =
                    new UserLookupPort.UserInfo(invitedId, "invited", "invited@mail.com");

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, inviterId))
                    .thenReturn(Optional.of(admin));
            when(userLookupPort.findByIdentifier("invited@mail.com"))
                    .thenReturn(Optional.of(invitedUser));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, invitedId))
                    .thenReturn(Optional.of(existingMember));

            InviteMemberUseCase.InviteMemberCommand command =
                    new InviteMemberUseCase.InviteMemberCommand("invited@mail.com");

            assertThatThrownBy(() ->
                    service.inviteMember(goalId, inviterId, command)
            ).isInstanceOf(ResourceAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Should throw when pending invitation already exists")
        void shouldThrowWhenPendingInvitationExists() {
            GoalUnit goal = createActiveGoal();
            GoalMember admin = createAdminMember();
            UserLookupPort.UserInfo invitedUser =
                    new UserLookupPort.UserInfo(invitedId, "invited", "invited@mail.com");
            GoalInvitation existingInvitation = GoalInvitation.create(
                    TIME,
                    goalId, inviterId, invitedId
            );

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, inviterId))
                    .thenReturn(Optional.of(admin));
            when(userLookupPort.findByIdentifier("invited@mail.com"))
                    .thenReturn(Optional.of(invitedUser));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, invitedId))
                    .thenReturn(Optional.empty());
            when(goalInvitationRepository.findByGoalIdAndInvitedUserIdAndStatus(
                    goalId, invitedId, InvitationStatus.PENDING
            )).thenReturn(Optional.of(existingInvitation));

            InviteMemberUseCase.InviteMemberCommand command =
                    new InviteMemberUseCase.InviteMemberCommand("invited@mail.com");

            assertThatThrownBy(() ->
                    service.inviteMember(goalId, inviterId, command)
            ).isInstanceOf(ResourceAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Should throw when inviter is not admin")
        void shouldThrowWhenInviterIsNotAdmin() {
            GoalUnit goal = createActiveGoal();
            GoalMember member = GoalMember.reconstitute(
                    UUID.randomUUID(), goalId, inviterId,
                    GoalRole.MEMBER, null, null,
                    Instant.now(), Instant.now(),
                    Instant.now(), true
            );

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, inviterId))
                    .thenReturn(Optional.of(member));

            InviteMemberUseCase.InviteMemberCommand command =
                    new InviteMemberUseCase.InviteMemberCommand("invited@mail.com");

            assertThatThrownBy(() ->
                    service.inviteMember(goalId, inviterId, command)
            ).isInstanceOf(ForbiddenOperationException.class);
        }
    }

    @Nested
    @DisplayName("acceptInvitation")
    class AcceptInvitation {

        @Test
        @DisplayName("Should accept valid invitation and create member")
        void shouldAcceptValidInvitationAndCreateMember() {
            GoalInvitation invitation = GoalInvitation.create(
                    TIME,
                    goalId, inviterId, invitedId
            );
            GoalUnit goal = createActiveGoal();

            when(goalInvitationRepository.findById(invitation.getId()))
                    .thenReturn(Optional.of(invitation));
            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, invitedId))
                    .thenReturn(Optional.empty());
            when(goalMemberRepository.save(any(GoalMember.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(goalInvitationRepository.save(any(GoalInvitation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            GoalInvitation result = service.acceptInvitation(invitedId, invitation.getId());

            assertThat(result.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);

            ArgumentCaptor<GoalMember> memberCaptor =
                    ArgumentCaptor.forClass(GoalMember.class);
            verify(goalMemberRepository).save(memberCaptor.capture());

            GoalMember newMember = memberCaptor.getValue();
            assertThat(newMember.getRole()).isEqualTo(GoalRole.MEMBER);
            assertThat(newMember.getUserId()).isEqualTo(invitedId);
            assertThat(newMember.isActive()).isTrue();

            verify(notificationPort).markInvitationNotificationAsActioned(
                    invitedId, invitation.getId()
            );
        }

        @Test
        @DisplayName("Should throw when invitation is not acceptable")
        void shouldThrowWhenInvitationNotAcceptable() {
            GoalInvitation invitation = GoalInvitation.create(
                    TIME,
                    goalId, inviterId, invitedId
            );
            invitation.markAsAccepted(TIME);

            when(goalInvitationRepository.findById(invitation.getId()))
                    .thenReturn(Optional.of(invitation));

            assertThatThrownBy(() ->
                    service.acceptInvitation(invitedId, invitation.getId())
            ).isInstanceOf(ForbiddenOperationException.class);
        }

        @Test
        @DisplayName("Should throw when not the invited user")
        void shouldThrowWhenNotInvitedUser() {
            GoalInvitation invitation = GoalInvitation.create(
                    TIME,
                    goalId, inviterId, invitedId
            );

            when(goalInvitationRepository.findById(invitation.getId()))
                    .thenReturn(Optional.of(invitation));

            UUID otherUserId = UUID.randomUUID();

            assertThatThrownBy(() ->
                    service.acceptInvitation(otherUserId, invitation.getId())
            ).isInstanceOf(ForbiddenOperationException.class);
        }
    }

    @Nested
    @DisplayName("rejectInvitation")
    class RejectInvitation {

        @Test
        @DisplayName("Should reject valid pending invitation")
        void shouldRejectValidPendingInvitation() {
            GoalInvitation invitation = GoalInvitation.create(
                    TIME,
                    goalId, inviterId, invitedId
            );

            when(goalInvitationRepository.findById(invitation.getId()))
                    .thenReturn(Optional.of(invitation));
            when(goalInvitationRepository.save(any(GoalInvitation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            service.rejectInvitation(invitedId, invitation.getId());

            assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.REJECTED);
            verify(goalInvitationRepository).save(invitation);
            verify(notificationPort).markInvitationNotificationAsActioned(
                    invitedId, invitation.getId()
            );
        }

        @Test
        @DisplayName("Should throw when invitation is not pending")
        void shouldThrowWhenInvitationNotPending() {
            GoalInvitation invitation = GoalInvitation.create(
                    TIME,
                    goalId, inviterId, invitedId
            );
            invitation.markAsAccepted(TIME);

            when(goalInvitationRepository.findById(invitation.getId()))
                    .thenReturn(Optional.of(invitation));

            assertThatThrownBy(() ->
                    service.rejectInvitation(invitedId, invitation.getId())
            ).isInstanceOf(ForbiddenOperationException.class);
        }
    }

    @Nested
    @DisplayName("listPendingInvitations")
    class ListPendingInvitations {

        @Test
        @DisplayName("Should list pending invitations with goal and inviter info")
        void shouldListPendingInvitations() {
            GoalInvitation invitation = GoalInvitation.create(
                    TIME,
                    goalId, inviterId, invitedId
            );
            GoalUnit goal = createActiveGoal();
            UserLookupPort.UserInfo inviterUser =
                    new UserLookupPort.UserInfo(inviterId, "inviter", "inviter@mail.com");

            when(goalInvitationRepository.findActiveAndNotExpiredByInvitedUserIdAndStatus(
                    invitedId, InvitationStatus.PENDING
            )).thenReturn(List.of(invitation));
            when(goalUnitRepository.findByIds(List.of(goalId)))
                    .thenReturn(List.of(goal));
            when(userLookupPort.findByIds(List.of(inviterId)))
                    .thenReturn(java.util.Map.of(inviterId, inviterUser));

            var result = service.listPendingInvitations(invitedId);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().goalName()).isEqualTo("Family trip");
            assertThat(result.getFirst().inviterName()).isEqualTo("inviter");
            assertThat(result.getFirst().status()).isEqualTo(InvitationStatus.PENDING);
        }

        @Test
        @DisplayName("Should return empty list when no pending invitations")
        void shouldReturnEmptyListWhenNoPendingInvitations() {
            when(goalInvitationRepository.findActiveAndNotExpiredByInvitedUserIdAndStatus(
                    invitedId, InvitationStatus.PENDING
            )).thenReturn(List.of());

            var result = service.listPendingInvitations(invitedId);

            assertThat(result).isEmpty();
        }
    }
}