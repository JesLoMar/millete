package com.puntomartinez.millete.groupgoals.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GoalMember aggregate")
class GoalMemberTest {

    private static final UUID GOAL_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    private GoalMember createValid() {
        return GoalMember.create(
                GOAL_ID,
                USER_ID,
                GoalRole.MEMBER,
                new BigDecimal("2000.00")
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Should create valid goal member")
        void shouldCreateValidGoalMember() {
            GoalMember member = createValid();

            assertThat(member.getId()).isNotNull();
            assertThat(member.getGoalId()).isEqualTo(GOAL_ID);
            assertThat(member.getUserId()).isEqualTo(USER_ID);
            assertThat(member.getRole()).isEqualTo(GoalRole.MEMBER);
            assertThat(member.getSalary()).isEqualByComparingTo("2000.00");
            assertThat(member.getCustomPercentage()).isNull();
            assertThat(member.isActive()).isTrue();
            assertThat(member.getJoinedAt()).isNotNull();
            assertThat(member.getCreatedAt()).isNotNull();
            assertThat(member.getModifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should create member with custom percentage")
        void shouldCreateMemberWithCustomPercentage() {
            GoalMember member = GoalMember.create(
                    GOAL_ID, USER_ID, GoalRole.MEMBER,
                    new BigDecimal("2000.00"), new BigDecimal("30.00")
            );

            assertThat(member.getCustomPercentage()).isEqualByComparingTo("30.00");
        }

        @Test
        @DisplayName("Should create admin member")
        void shouldCreateAdminMember() {
            GoalMember member = GoalMember.create(
                    GOAL_ID, USER_ID, GoalRole.ADMIN, null
            );

            assertThat(member.getRole()).isEqualTo(GoalRole.ADMIN);
            assertThat(member.isAdmin()).isTrue();
        }

        @Test
        @DisplayName("Should reject null goal id")
        void shouldRejectNullGoalId() {
            assertThatThrownBy(() -> GoalMember.create(
                    null, USER_ID, GoalRole.MEMBER, null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject null user id")
        void shouldRejectNullUserId() {
            assertThatThrownBy(() -> GoalMember.create(
                    GOAL_ID, null, GoalRole.MEMBER, null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject null role")
        void shouldRejectNullRole() {
            assertThatThrownBy(() -> GoalMember.create(
                    GOAL_ID, USER_ID, null, null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject negative salary")
        void shouldRejectNegativeSalary() {
            assertThatThrownBy(() -> GoalMember.create(
                    GOAL_ID, USER_ID, GoalRole.MEMBER, new BigDecimal("-10.00")
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject custom percentage below zero")
        void shouldRejectCustomPercentageBelowZero() {
            assertThatThrownBy(() -> GoalMember.create(
                    GOAL_ID, USER_ID, GoalRole.MEMBER,
                    null, new BigDecimal("-5.00")
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject custom percentage above 100")
        void shouldRejectCustomPercentageAbove100() {
            assertThatThrownBy(() -> GoalMember.create(
                    GOAL_ID, USER_ID, GoalRole.MEMBER,
                    null, new BigDecimal("105.00")
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should allow custom percentage at 100")
        void shouldAllowCustomPercentageAt100() {
            GoalMember member = GoalMember.create(
                    GOAL_ID, USER_ID, GoalRole.MEMBER,
                    null, new BigDecimal("100.00")
            );

            assertThat(member.getCustomPercentage()).isEqualByComparingTo("100.00");
        }
    }

    @Nested
    @DisplayName("updateDetails")
    class UpdateDetails {

        @Test
        @DisplayName("Should update all details")
        void shouldUpdateAllDetails() {
            GoalMember member = createValid();
            LocalDateTime previousModifiedAt = member.getModifiedAt();

            member.updateDetails(
                    GoalRole.ADMIN,
                    new BigDecimal("3000.00"),
                    new BigDecimal("40.00")
            );

            assertThat(member.getRole()).isEqualTo(GoalRole.ADMIN);
            assertThat(member.getSalary()).isEqualByComparingTo("3000.00");
            assertThat(member.getCustomPercentage()).isEqualByComparingTo("40.00");
            assertThat(member.getModifiedAt()).isAfterOrEqualTo(previousModifiedAt);
        }

        @Test
        @DisplayName("Should not update null fields")
        void shouldNotUpdateNullFields() {
            GoalMember member = createValid();

            member.updateDetails(null, null, null);

            assertThat(member.getRole()).isEqualTo(GoalRole.MEMBER);
            assertThat(member.getSalary()).isEqualByComparingTo("2000.00");
        }
    }

    @Nested
    @DisplayName("activate and deactivate")
    class ActivateDeactivate {

        @Test
        @DisplayName("Should activate inactive member")
        void shouldActivateInactiveMember() {
            GoalMember member = createValid();
            member.deactivate();

            member.activate();

            assertThat(member.isActive()).isTrue();
            assertThat(member.getJoinedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should deactivate active member")
        void shouldDeactivateActiveMember() {
            GoalMember member = createValid();

            member.deactivate();

            assertThat(member.isActive()).isFalse();
            assertThat(member.getModifiedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("isAdmin")
    class IsAdmin {

        @Test
        @DisplayName("Should return true for admin role")
        void shouldReturnTrueForAdminRole() {
            GoalMember member = GoalMember.create(
                    GOAL_ID, USER_ID, GoalRole.ADMIN, null
            );

            assertThat(member.isAdmin()).isTrue();
        }

        @Test
        @DisplayName("Should return false for member role")
        void shouldReturnFalseForMemberRole() {
            GoalMember member = createValid();

            assertThat(member.isAdmin()).isFalse();
        }
    }
}