package com.puntomartinez.millete.groupgoals.domain.model;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GoalContribution aggregate")
class GoalContributionTest {

    private static final UUID GOAL_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    private GoalContribution createValid() {
        return GoalContribution.create(
                GOAL_ID,
                USER_ID,
                new BigDecimal("100.00"),
                ContributionType.DEPOSIT,
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Should create valid contribution")
        void shouldCreateValidContribution() {
            GoalContribution contribution = createValid();

            assertThat(contribution.getId()).isNotNull();
            assertThat(contribution.getGoalId()).isEqualTo(GOAL_ID);
            assertThat(contribution.getUserId()).isEqualTo(USER_ID);
            assertThat(contribution.getAmount()).isEqualByComparingTo("100.00");
            assertThat(contribution.getType()).isEqualTo(ContributionType.DEPOSIT);
            assertThat(contribution.getDate()).isNotNull();
            assertThat(contribution.isActive()).isTrue();
            assertThat(contribution.getCreatedAt()).isNotNull();
            assertThat(contribution.getModifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should create withdrawal contribution")
        void shouldCreateWithdrawalContribution() {
            GoalContribution contribution = GoalContribution.create(
                    GOAL_ID, USER_ID, new BigDecimal("50.00"),
                    ContributionType.WITHDRAWAL, LocalDateTime.now()
            );

            assertThat(contribution.getType()).isEqualTo(ContributionType.WITHDRAWAL);
        }

        @Test
        @DisplayName("Should reject null goal id")
        void shouldRejectNullGoalId() {
            assertThatThrownBy(() -> GoalContribution.create(
                    null, USER_ID, new BigDecimal("100.00"),
                    ContributionType.DEPOSIT, LocalDateTime.now()
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null user id")
        void shouldRejectNullUserId() {
            assertThatThrownBy(() -> GoalContribution.create(
                    GOAL_ID, null, new BigDecimal("100.00"),
                    ContributionType.DEPOSIT, LocalDateTime.now()
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null amount")
        void shouldRejectNullAmount() {
            assertThatThrownBy(() -> GoalContribution.create(
                    GOAL_ID, USER_ID, null,
                    ContributionType.DEPOSIT, LocalDateTime.now()
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject zero amount")
        void shouldRejectZeroAmount() {
            assertThatThrownBy(() -> GoalContribution.create(
                    GOAL_ID, USER_ID, BigDecimal.ZERO,
                    ContributionType.DEPOSIT, LocalDateTime.now()
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject negative amount")
        void shouldRejectNegativeAmount() {
            assertThatThrownBy(() -> GoalContribution.create(
                    GOAL_ID, USER_ID, new BigDecimal("-10.00"),
                    ContributionType.DEPOSIT, LocalDateTime.now()
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null type")
        void shouldRejectNullType() {
            assertThatThrownBy(() -> GoalContribution.create(
                    GOAL_ID, USER_ID, new BigDecimal("100.00"),
                    null, LocalDateTime.now()
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null date")
        void shouldRejectNullDate() {
            assertThatThrownBy(() -> GoalContribution.create(
                    GOAL_ID, USER_ID, new BigDecimal("100.00"),
                    ContributionType.DEPOSIT, null
            )).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("reconstitute")
    class Reconstitute {

        @Test
        @DisplayName("Should reconstitute existing contribution")
        void shouldReconstituteContribution() {
            UUID id = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 10, 0);
            LocalDateTime date = LocalDateTime.of(2024, 1, 1, 10, 0);

            GoalContribution contribution = GoalContribution.reconstitute(
                    id, GOAL_ID, USER_ID, new BigDecimal("100.00"),
                    ContributionType.DEPOSIT, date,
                    createdAt, modifiedAt, true
            );

            assertThat(contribution.getId()).isEqualTo(id);
            assertThat(contribution.getAmount()).isEqualByComparingTo("100.00");
            assertThat(contribution.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should reject null id on reconstitute")
        void shouldRejectNullIdOnReconstitute() {
            assertThatThrownBy(() -> GoalContribution.reconstitute(
                    null, GOAL_ID, USER_ID, new BigDecimal("100.00"),
                    ContributionType.DEPOSIT, LocalDateTime.now(),
                    LocalDateTime.now(), LocalDateTime.now(), true
            )).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("Should deactivate contribution")
        void shouldDeactivateContribution() {
            GoalContribution contribution = createValid();

            contribution.deactivate();

            assertThat(contribution.isActive()).isFalse();
            assertThat(contribution.getModifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should not change modified at when already inactive")
        void shouldNotChangeModifiedAtWhenAlreadyInactive() {
            GoalContribution contribution = createValid();
            contribution.deactivate();
            LocalDateTime previousModifiedAt = contribution.getModifiedAt();

            contribution.deactivate();

            assertThat(contribution.getModifiedAt()).isEqualTo(previousModifiedAt);
        }
    }
}