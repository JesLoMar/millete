package com.puntomartinez.millete.groupgoals.application.services;

import com.puntomartinez.millete.groupgoals.domain.model.*;
import com.puntomartinez.millete.groupgoals.domain.ports.in.GetContributionHistoryUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.ListGoalsUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalContributionRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalMemberRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalUnitRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.UserLookupPort;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import com.puntomartinez.millete.shared.domain.time.FixedTimeProvider;
import org.mockito.Spy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupGoalQueryService")
class GroupGoalQueryServiceTest {
    static final TimeProvider TIME = new FixedTimeProvider(
            Instant.parse("2024-01-15T10:00:00Z"));


    @Mock
    private GoalUnitRepository goalUnitRepository;

    @Mock
    private GoalMemberRepository goalMemberRepository;

    @Mock
    private GoalContributionRepository goalContributionRepository;

    @Mock
    private UserLookupPort userLookupPort;

    private final TimeProvider TIME =
            new FixedTimeProvider(Instant.parse("2024-01-01T10:00:00Z"));

    private GroupGoalQueryService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID goalId = UUID.randomUUID();

    @Nested
    @DisplayName("listGoals")
    class ListGoals {

        @Test
        @DisplayName("Should list goals for user")
        void shouldListGoalsForUser() {
            GoalUnit goal = GoalUnit.reconstitute(
                    goalId, "Family trip", new BigDecimal("300.00"),
                    DistributionMode.EQUITATIVE,
                    Instant.now(), Instant.now(), true
            );
            GoalMember membership = GoalMember.reconstitute(
                    UUID.randomUUID(), goalId, userId,
                    GoalRole.MEMBER, null, null,
                    Instant.now(), Instant.now(),
                    Instant.now(), true
            );

            when(goalMemberRepository.findActiveByUserId(userId))
                    .thenReturn(List.of(membership));
            when(goalUnitRepository.findByIds(List.of(goalId)))
                    .thenReturn(List.of(goal));

            List<ListGoalsUseCase.GoalSummary> result =
                    service.listGoals(userId);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().name()).isEqualTo("Family trip");
            assertThat(result.getFirst().admin()).isFalse();
        }

        @Test
        @DisplayName("Should return empty list when no memberships")
        void shouldReturnEmptyListWhenNoMemberships() {
            when(goalMemberRepository.findActiveByUserId(userId))
                    .thenReturn(List.of());

            List<ListGoalsUseCase.GoalSummary> result =
                    service.listGoals(userId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("calculateContributions")
    class CalculateContributions {

        @Test
        @DisplayName("Should calculate equitative contributions")
        void shouldCalculateEquitativeContributions() {
            GoalUnit goal = GoalUnit.reconstitute(
                    goalId, "Family trip", new BigDecimal("300.00"),
                    DistributionMode.EQUITATIVE,
                    Instant.now(), Instant.now(), true
            );
            GoalMember requester = GoalMember.reconstitute(
                    UUID.randomUUID(), goalId, userId,
                    GoalRole.ADMIN, null, null,
                    Instant.now(), Instant.now(),
                    Instant.now(), true
            );
            GoalMember member2 = GoalMember.reconstitute(
                    UUID.randomUUID(), goalId, UUID.randomUUID(),
                    GoalRole.MEMBER, null, null,
                    Instant.now(), Instant.now(),
                    Instant.now(), true
            );

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId))
                    .thenReturn(Optional.of(requester));
            when(goalMemberRepository.findActiveByGoalId(goalId))
                    .thenReturn(List.of(requester, member2));

            var result = service.calculateContributions(goalId, userId);

            assertThat(result.monthlyTarget()).isEqualByComparingTo("300.00");
            assertThat(result.distributionMode()).isEqualTo(DistributionMode.EQUITATIVE);
            assertThat(result.contributions()).hasSize(2);
            assertThat(result.contributions().getFirst().suggestedAmount())
                    .isEqualByComparingTo("150.00");
        }

        @Test
        @DisplayName("Should calculate proportional contributions")
        void shouldCalculateProportionalContributions() {
            UUID member2Id = UUID.randomUUID();
            GoalUnit goal = GoalUnit.reconstitute(
                    goalId, "Family trip", new BigDecimal("300.00"),
                    DistributionMode.PROPORTIONAL,
                    Instant.now(), Instant.now(), true
            );
            GoalMember requester = GoalMember.reconstitute(
                    UUID.randomUUID(), goalId, userId,
                    GoalRole.ADMIN, new BigDecimal("2000.00"), null,
                    Instant.now(), Instant.now(),
                    Instant.now(), true
            );
            GoalMember member2 = GoalMember.reconstitute(
                    UUID.randomUUID(), goalId, member2Id,
                    GoalRole.MEMBER, new BigDecimal("1000.00"), null,
                    Instant.now(), Instant.now(),
                    Instant.now(), true
            );

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId))
                    .thenReturn(Optional.of(requester));
            when(goalMemberRepository.findActiveByGoalId(goalId))
                    .thenReturn(List.of(requester, member2));

            var result = service.calculateContributions(goalId, userId);

            assertThat(result.contributions()).hasSize(2);
            // requester: 2000/3000 * 300 = 200
            assertThat(result.contributions().get(0).suggestedAmount())
                    .isEqualByComparingTo("200.00");
            // member2: 1000/3000 * 300 = 100
            assertThat(result.contributions().get(1).suggestedAmount())
                    .isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("Should throw for proportional mode without salaries")
        void shouldThrowForProportionalWithoutSalaries() {
            GoalUnit goal = GoalUnit.reconstitute(
                    goalId, "Family trip", new BigDecimal("300.00"),
                    DistributionMode.PROPORTIONAL,
                    Instant.now(), Instant.now(), true
            );
            GoalMember requester = GoalMember.reconstitute(
                    UUID.randomUUID(), goalId, userId,
                    GoalRole.ADMIN, null, null,
                    Instant.now(), Instant.now(),
                    Instant.now(), true
            );

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId))
                    .thenReturn(Optional.of(requester));
            when(goalMemberRepository.findActiveByGoalId(goalId))
                    .thenReturn(List.of(requester));

            assertThatThrownBy(() ->
                    service.calculateContributions(goalId, userId)
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should throw for custom mode without percentages")
        void shouldThrowForCustomWithoutPercentages() {
            GoalUnit goal = GoalUnit.reconstitute(
                    goalId, "Family trip", new BigDecimal("300.00"),
                    DistributionMode.CUSTOM,
                    Instant.now(), Instant.now(), true
            );
            GoalMember requester = GoalMember.reconstitute(
                    UUID.randomUUID(), goalId, userId,
                    GoalRole.ADMIN, null, null,
                    Instant.now(), Instant.now(),
                    Instant.now(), true
            );

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId))
                    .thenReturn(Optional.of(requester));
            when(goalMemberRepository.findActiveByGoalId(goalId))
                    .thenReturn(List.of(requester));

            assertThatThrownBy(() ->
                    service.calculateContributions(goalId, userId)
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should throw when user is not a member")
        void shouldThrowWhenUserIsNotMember() {
            GoalUnit goal = GoalUnit.reconstitute(
                    goalId, "Family trip", new BigDecimal("300.00"),
                    DistributionMode.EQUITATIVE,
                    Instant.now(), Instant.now(), true
            );

            when(goalUnitRepository.findById(goalId))
                    .thenReturn(Optional.of(goal));
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    service.calculateContributions(goalId, userId)
            ).isInstanceOf(ForbiddenOperationException.class);
        }
    }

    @Nested
    @DisplayName("getContributionHistory")
    class GetContributionHistory {

        @Test
        @DisplayName("Should return paginated contributions")
        void shouldReturnPaginatedContributions() {
            GoalMember member = GoalMember.reconstitute(
                    UUID.randomUUID(), goalId, userId,
                    GoalRole.MEMBER, null, null,
                    Instant.now(), Instant.now(),
                    Instant.now(), true
            );

            GoalContribution contribution = GoalContribution.create(TIME, 
                    TIME,
                    goalId, userId, new BigDecimal("100.00"),
                    ContributionType.DEPOSIT, Instant.now()
            );

            GetContributionHistoryUseCase.PaginatedContributions paginated =
                    new GetContributionHistoryUseCase.PaginatedContributions(
                            List.of(contribution), 0, 1, 1, 10, true, true
                    );

            when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId))
                    .thenReturn(Optional.of(member));
            when(goalContributionRepository.findByGoalId(goalId, 0, 10))
                    .thenReturn(paginated);

            var result = service.getContributionHistory(goalId, userId, 0, 10);

            assertThat(result.contributions()).hasSize(1);
            assertThat(result.currentPage()).isZero();
        }

        @Test
        @DisplayName("Should throw when user is not a member")
        void shouldThrowWhenUserIsNotMemberForHistory() {
            when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    service.getContributionHistory(goalId, userId, 0, 10)
            ).isInstanceOf(ForbiddenOperationException.class);
        }
    }
}