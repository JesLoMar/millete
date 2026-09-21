package com.puntomartinez.millete.groupgoals.domain.model;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GoalUnit aggregate")
class GoalUnitTest {

    private GoalUnit createValid() {
        return GoalUnit.create(
                "Family trip",
                new BigDecimal("300.00"),
                DistributionMode.EQUITATIVE
        );
    }

    private GoalUnit reconstituteValid(boolean active) {
        return GoalUnit.reconstitute(
                UUID.randomUUID(),
                "Family trip",
                new BigDecimal("300.00"),
                DistributionMode.EQUITATIVE,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                LocalDateTime.of(2024, 1, 2, 10, 0),
                active
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Should create valid goal unit")
        void shouldCreateValidGoalUnit() {
            GoalUnit goal = createValid();

            assertThat(goal.getId()).isNotNull();
            assertThat(goal.getName()).isEqualTo("Family trip");
            assertThat(goal.getMonthlyTarget()).isEqualByComparingTo("300.00");
            assertThat(goal.getDistributionMode()).isEqualTo(DistributionMode.EQUITATIVE);
            assertThat(goal.isActive()).isTrue();
            assertThat(goal.getCreatedAt()).isNotNull();
            assertThat(goal.getModifiedAt()).isNotNull();
            assertThat(goal.getCreatedAt()).isEqualTo(goal.getModifiedAt());
        }

        @Test
        @DisplayName("Should generate different ids for different goals")
        void shouldGenerateDifferentIds() {
            GoalUnit first = createValid();
            GoalUnit second = createValid();

            assertThat(first.getId()).isNotEqualTo(second.getId());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Should reject blank name")
        void shouldRejectBlankName(String name) {
            assertThatThrownBy(() -> GoalUnit.create(
                    name, new BigDecimal("100.00"), DistributionMode.EQUITATIVE
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject name exceeding max length")
        void shouldRejectNameExceedingMaxLength() {
            String longName = "A".repeat(101);

            assertThatThrownBy(() -> GoalUnit.create(
                    longName, new BigDecimal("100.00"), DistributionMode.EQUITATIVE
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should allow name at max length")
        void shouldAllowNameAtMaxLength() {
            String maxName = "A".repeat(100);

            GoalUnit goal = GoalUnit.create(
                    maxName, new BigDecimal("100.00"), DistributionMode.EQUITATIVE
            );

            assertThat(goal.getName()).hasSize(100);
        }

        @Test
        @DisplayName("Should reject null monthly target")
        void shouldRejectNullMonthlyTarget() {
            assertThatThrownBy(() -> GoalUnit.create(
                    "Family trip", null, DistributionMode.EQUITATIVE
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject negative monthly target")
        void shouldRejectNegativeMonthlyTarget() {
            assertThatThrownBy(() -> GoalUnit.create(
                    "Family trip", new BigDecimal("-10.00"), DistributionMode.EQUITATIVE
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should allow zero monthly target")
        void shouldAllowZeroMonthlyTarget() {
            GoalUnit goal = GoalUnit.create(
                    "Family trip", BigDecimal.ZERO, DistributionMode.EQUITATIVE
            );

            assertThat(goal.getMonthlyTarget()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should reject null distribution mode")
        void shouldRejectNullDistributionMode() {
            assertThatThrownBy(() -> GoalUnit.create(
                    "Family trip", new BigDecimal("100.00"), null
            )).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("reconstitute")
    class Reconstitute {

        @Test
        @DisplayName("Should reconstitute existing goal unit")
        void shouldReconstituteGoalUnit() {
            UUID id = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 10, 0);

            GoalUnit goal = GoalUnit.reconstitute(
                    id, "Family trip", new BigDecimal("300.00"),
                    DistributionMode.PROPORTIONAL, createdAt, modifiedAt, false
            );

            assertThat(goal.getId()).isEqualTo(id);
            assertThat(goal.getName()).isEqualTo("Family trip");
            assertThat(goal.getDistributionMode()).isEqualTo(DistributionMode.PROPORTIONAL);
            assertThat(goal.isActive()).isFalse();
            assertThat(goal.getCreatedAt()).isEqualTo(createdAt);
            assertThat(goal.getModifiedAt()).isEqualTo(modifiedAt);
        }

        @Test
        @DisplayName("Should reject null id on reconstitute")
        void shouldRejectNullIdOnReconstitute() {
            assertThatThrownBy(() -> GoalUnit.reconstitute(
                    null, "Family trip", new BigDecimal("300.00"),
                    DistributionMode.EQUITATIVE,
                    LocalDateTime.now(), LocalDateTime.now(), true
            )).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("updateDetails")
    class UpdateDetails {

        @Test
        @DisplayName("Should update all details")
        void shouldUpdateAllDetails() {
            GoalUnit goal = reconstituteValid(true);
            LocalDateTime previousModifiedAt = goal.getModifiedAt();

            goal.updateDetails(
                    "Updated trip",
                    new BigDecimal("500.00"),
                    DistributionMode.CUSTOM
            );

            assertThat(goal.getName()).isEqualTo("Updated trip");
            assertThat(goal.getMonthlyTarget()).isEqualByComparingTo("500.00");
            assertThat(goal.getDistributionMode()).isEqualTo(DistributionMode.CUSTOM);
            assertThat(goal.getModifiedAt()).isAfter(previousModifiedAt);
        }

        @Test
        @DisplayName("Should not update null fields")
        void shouldNotUpdateNullFields() {
            GoalUnit goal = reconstituteValid(true);

            goal.updateDetails(null, null, null);

            assertThat(goal.getName()).isEqualTo("Family trip");
            assertThat(goal.getMonthlyTarget()).isEqualByComparingTo("300.00");
            assertThat(goal.getDistributionMode()).isEqualTo(DistributionMode.EQUITATIVE);
        }

        @Test
        @DisplayName("Should reject invalid name on update")
        void shouldRejectInvalidNameOnUpdate() {
            GoalUnit goal = reconstituteValid(true);

            assertThatThrownBy(() -> goal.updateDetails(
                    "", null, null
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject negative monthly target on update")
        void shouldRejectNegativeMonthlyTargetOnUpdate() {
            GoalUnit goal = reconstituteValid(true);

            assertThatThrownBy(() -> goal.updateDetails(
                    null, new BigDecimal("-10.00"), null
            )).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("Should deactivate goal unit")
        void shouldDeactivateGoalUnit() {
            GoalUnit goal = reconstituteValid(true);

            goal.deactivate();

            assertThat(goal.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should not change modified at when already inactive")
        void shouldNotChangeModifiedAtWhenAlreadyInactive() {
            GoalUnit goal = reconstituteValid(false);
            LocalDateTime previousModifiedAt = goal.getModifiedAt();

            goal.deactivate();

            assertThat(goal.isActive()).isFalse();
            assertThat(goal.getModifiedAt()).isEqualTo(previousModifiedAt);
        }
    }
}