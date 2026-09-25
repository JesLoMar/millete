package com.puntomartinez.millete.groupgoals.application.services;

import com.puntomartinez.millete.groupgoals.domain.model.*;
import com.puntomartinez.millete.groupgoals.domain.ports.in.*;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalContributionRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalInvitationRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalMemberRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalUnitRepository;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
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
@DisplayName("GroupGoalCommandService")
class GroupGoalCommandServiceTest {
    static final TimeProvider TIME = new FixedTimeProvider(
            Instant.parse("2024-01-15T10:00:00Z"));


    @Mock
    private GoalUnitRepository goalUnitRepository;

    @Mock
    private GoalMemberRepository goalMemberRepository;

    @Mock
    private GoalContributionRepository goalContributionRepository;

    @Mock
    private GoalInvitationRepository goalInvitationRepository;

    private final TimeProvider TIME =
            new FixedTimeProvider(Instant.parse("2024-01-01T10:00:00Z"));

    private GroupGoalCommandService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID otherUserId = UUID.randomUUID();
    private final UUID goalId = UUID.randomUUID();

    private GoalUnit createActiveGoal() {
        return GoalUnit.reconstitute(
                goalId, "Family trip", new BigDecimal("300.00"),
                DistributionMode.EQUITATIVE,
                Instant.now(), Instant.now(), true
        );
    }

    private GoalMember createAdminMember() {
        return GoalMember.reconstitute(
                UUID.randomUUID(), goalId, userId,
                GoalRole.ADMIN, new BigDecimal("2000.00"), null,
                Instant.now(), Instant.now(),
                Instant.now(), true
        );
    }

    private GoalMember createRegularMember(UUID memberUserId) {
        return GoalMember.reconstitute(
                UUID.randomUUID(), goalId, memberUserId,
                GoalRole.MEMBER, new BigDecimal("2000.00"), null,
                Instant.now(), Instant.now(),
                Instant.now(), true
        );
    }


    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new GroupGoalCommandService(
                goalUnitRepository, goalMemberRepository, goalContributionRepository, goalInvitationRepository, TIME
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Should create goal unit and admin member")
        void shouldCreateGoalUnitAndAdminMember() {
            CreateGoalUnitUseCase.CreateGoalUnitCommand command =
                    new CreateGoalUnitUseCase.CreateGoalUnitCommand(
                            userId, "Family trip",
                            new BigDecimal("300.00"),
                            DistributionMode.EQUITATIVE
                    );

            GoalUnit savedGoal = createActiveGoal();
            when(goalUnitRepository.save(any(GoalUnit.class)))
                    .thenReturn(savedGoal);
            when(goalMemberRepository.save(any(GoalMember.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            GoalUnit result = service.create(command);

            assertThat(result.getName()).isEqualTo("Family trip");

            ArgumentCaptor<GoalMember> memberCaptor =
                    ArgumentCaptor.forClass(GoalMember.class);
            verify(goalMemberRepository).save(memberCaptor.capture());

            GoalMember savedMember = memberCaptor.getValue();
            assertThat(savedMember.getRole()).isEqualTo(GoalRole.ADMIN);
            assertThat(savedMember.getUserId()).isEqualTo(userId);
            assertThat(savedMember.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Should update goal when user is admin")
        void shouldUpdateGoalWhenUserIsAdmin() {
            GoalUnit goal = createActiveGoal();
            GoalMember admin = createAdminMember();

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId))
                    .thenReturn(Optional.of(admin));
            when(goalUnitRepository.save(any(GoalUnit.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            UpdateGoalUseCase.UpdateGoalCommand command =
                    new UpdateGoalUseCase.UpdateGoalCommand(
                            "Updated trip",
                            new BigDecimal("500.00"),
                            DistributionMode.CUSTOM
                    );

            GoalUnit result = service.update(goalId, userId, command);

            assertThat(result.getName()).isEqualTo("Updated trip");
            assertThat(result.getMonthlyTarget()).isEqualByComparingTo("500.00");
            verify(goalUnitRepository).save(goal);
        }

        @Test
        @DisplayName("Should throw when goal not found")
        void shouldThrowWhenGoalNotFound() {
            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.empty());

            UpdateGoalUseCase.UpdateGoalCommand command =
                    new UpdateGoalUseCase.UpdateGoalCommand(
                            "Updated", new BigDecimal("100.00"),
                            DistributionMode.EQUITATIVE
                    );

            assertThatThrownBy(() -> service.update(goalId, userId, command))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw when user is not admin")
        void shouldThrowWhenUserIsNotAdmin() {
            GoalUnit goal = createActiveGoal();
            GoalMember member = createRegularMember(userId);

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId))
                    .thenReturn(Optional.of(member));

            UpdateGoalUseCase.UpdateGoalCommand command =
                    new UpdateGoalUseCase.UpdateGoalCommand(
                            "Updated", new BigDecimal("100.00"),
                            DistributionMode.EQUITATIVE
                    );

            assertThatThrownBy(() -> service.update(goalId, userId, command))
                    .isInstanceOf(ForbiddenOperationException.class);
        }
    }

    @Nested
    @DisplayName("deleteGoalUnit")
    class DeleteGoalUnit {

        @Test
        @DisplayName("Should deactivate goal and related entities")
        void shouldDeactivateGoalAndRelatedEntities() {
            GoalUnit goal = createActiveGoal();
            GoalMember admin = createAdminMember();

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId))
                    .thenReturn(Optional.of(admin));
            when(goalUnitRepository.save(any(GoalUnit.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            service.deleteGoalUnit(goalId, userId);

            assertThat(goal.isActive()).isFalse();
            verify(goalUnitRepository).save(goal);
            verify(goalMemberRepository).deactivateByGoalId(goalId);
            verify(goalInvitationRepository).deactivatePendingByGoalId(goalId);
            verify(goalContributionRepository).deactivateByGoalId(goalId);
        }
    }

    @Nested
    @DisplayName("deleteMember")
    class DeleteMember {

        @Test
        @DisplayName("Should deactivate member")
        void shouldDeactivateMember() {
            GoalUnit goal = createActiveGoal();
            GoalMember admin = createAdminMember();
            GoalMember targetMember = createRegularMember(otherUserId);

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId))
                    .thenReturn(Optional.of(admin));
            when(goalMemberRepository.findById(targetMember.getId()))
                    .thenReturn(Optional.of(targetMember));

            service.deleteMember(goalId, targetMember.getId(), userId);

            assertThat(targetMember.isActive()).isFalse();
            verify(goalMemberRepository).save(targetMember);
        }

        @Test
        @DisplayName("Should prevent removing last admin")
        void shouldPreventRemovingLastAdmin() {
            GoalUnit goal = createActiveGoal();
            GoalMember admin = createAdminMember();

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId))
                    .thenReturn(Optional.of(admin));
            when(goalMemberRepository.findById(admin.getId()))
                    .thenReturn(Optional.of(admin));

            assertThatThrownBy(() ->
                    service.deleteMember(goalId, admin.getId(), userId)
            ).isInstanceOf(InvalidInputException.class);

            verify(goalMemberRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateMember")
    class UpdateMember {

        @Test
        @DisplayName("Should update member details when user is admin")
        void shouldUpdateMemberDetailsWhenUserIsAdmin() {
            GoalUnit goal = createActiveGoal();
            GoalMember admin = createAdminMember();
            GoalMember targetMember = createRegularMember(otherUserId);

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId))
                    .thenReturn(Optional.of(admin));
            when(goalMemberRepository.findById(targetMember.getId()))
                    .thenReturn(Optional.of(targetMember));
            when(goalMemberRepository.save(any(GoalMember.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            UpdateMemberUseCase.UpdateMemberCommand command =
                    new UpdateMemberUseCase.UpdateMemberCommand(
                            GoalRole.ADMIN, new BigDecimal("3000.00"), null
                    );

            GoalMember result = service.updateMember(goalId, targetMember.getId(), userId, command);

            assertThat(result.getRole()).isEqualTo(GoalRole.ADMIN);
            assertThat(result.getSalary()).isEqualByComparingTo("3000.00");
            verify(goalMemberRepository).save(targetMember);
        }

        @Test
        @DisplayName("Should throw when member not found")
        void shouldThrowWhenMemberNotFound() {
            GoalUnit goal = createActiveGoal();
            GoalMember admin = createAdminMember();
            UUID targetMemberId = UUID.randomUUID();

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId))
                    .thenReturn(Optional.of(admin));
            when(goalMemberRepository.findById(targetMemberId))
                    .thenReturn(Optional.empty());

            UpdateMemberUseCase.UpdateMemberCommand command =
                    new UpdateMemberUseCase.UpdateMemberCommand(
                            GoalRole.MEMBER, null, null
                    );

            assertThatThrownBy(() ->
                    service.updateMember(goalId, targetMemberId, userId, command)
            ).isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("addContribution")
    class AddContribution {

        @Test
        @DisplayName("Should add contribution when member is active")
        void shouldAddContributionWhenMemberIsActive() {
            GoalUnit goal = createActiveGoal();
            GoalMember member = createRegularMember(userId);

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId))
                    .thenReturn(Optional.of(member));
            when(goalContributionRepository.save(any(GoalContribution.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AddContributionUseCase.AddContributionCommand command =
                    new AddContributionUseCase.AddContributionCommand(
                            new BigDecimal("100.00")
                    );

            service.addContribution(goalId, userId, command);

            ArgumentCaptor<GoalContribution> captor =
                    ArgumentCaptor.forClass(GoalContribution.class);
            verify(goalContributionRepository).save(captor.capture());

            GoalContribution saved = captor.getValue();
            assertThat(saved.getAmount()).isEqualByComparingTo("100.00");
            assertThat(saved.getType()).isEqualTo(ContributionType.DEPOSIT);
            assertThat(saved.getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should throw when member is not active")
        void shouldThrowWhenMemberIsNotActive() {
            GoalUnit goal = createActiveGoal();
            GoalMember inactiveMember = createRegularMember(userId);
            inactiveMember.deactivate(TIME);

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId))
                    .thenReturn(Optional.of(inactiveMember));

            AddContributionUseCase.AddContributionCommand command =
                    new AddContributionUseCase.AddContributionCommand(
                            new BigDecimal("100.00")
                    );

            assertThatThrownBy(() ->
                    service.addContribution(goalId, userId, command)
            ).isInstanceOf(ForbiddenOperationException.class);
        }
    }

    @Nested
    @DisplayName("withdrawContribution")
    class WithdrawContribution {

        @Test
        @DisplayName("Should withdraw contribution when member is active")
        void shouldWithdrawContributionWhenMemberIsActive() {
            GoalUnit goal = createActiveGoal();
            GoalMember member = createAdminMember();

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId))
                    .thenReturn(Optional.of(member));
            when(goalContributionRepository.save(any(GoalContribution.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            WithdrawContributionUseCase.WithdrawContributionCommand command =
                    new WithdrawContributionUseCase.WithdrawContributionCommand(
                            new BigDecimal("50.00")
                    );

            service.withdrawContribution(goalId, userId, command);

            ArgumentCaptor<GoalContribution> captor =
                    ArgumentCaptor.forClass(GoalContribution.class);
            verify(goalContributionRepository).save(captor.capture());

            GoalContribution saved = captor.getValue();
            assertThat(saved.getAmount()).isEqualByComparingTo("50.00");
            assertThat(saved.getType()).isEqualTo(ContributionType.WITHDRAWAL);
            assertThat(saved.getUserId()).isEqualTo(userId);
        }
    }

    @Nested
    @DisplayName("leaveGoal")
    class LeaveGoal {

        @Test
        @DisplayName("Should prevent last admin from leaving")
        void shouldPreventLastAdminFromLeaving() {
            GoalUnit goal = createActiveGoal();
            GoalMember admin = createAdminMember();

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId))
                    .thenReturn(Optional.of(admin));
            when(goalMemberRepository.findActiveByGoalId(goalId))
                    .thenReturn(List.of(admin));

            assertThatThrownBy(() -> service.leaveGoal(goalId, userId))
                    .isInstanceOf(InvalidInputException.class);

            verify(goalMemberRepository, never()).save(any());
        }
    }
}