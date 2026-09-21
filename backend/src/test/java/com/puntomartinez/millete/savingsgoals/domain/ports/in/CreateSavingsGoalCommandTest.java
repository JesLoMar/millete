package com.puntomartinez.millete.savingsgoals.domain.ports.in;

import com.puntomartinez.millete.savingsgoals.domain.utils.GoalPriority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CreateSavingsGoalCommand")
class CreateSavingsGoalCommandTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    @DisplayName("Should create valid command")
    void shouldCreateValidCommand() {
        assertThatCode(() ->
                new CreateSavingsGoalCommand(
                        USER_ID,
                        "Goal",
                        BigDecimal.TEN,
                        LocalDate.now().plusDays(1),
                        GoalPriority.LOW,
                        null
                )
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should allow null deadline")
    void shouldAllowNullDeadline() {
        assertThatCode(() ->
                new CreateSavingsGoalCommand(
                        USER_ID,
                        "Goal",
                        BigDecimal.TEN,
                        null,
                        GoalPriority.LOW,
                        null
                )
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject null user id")
    void shouldRejectNullUserId() {
        assertThatThrownBy(() ->
                new CreateSavingsGoalCommand(
                        null,
                        "Goal",
                        BigDecimal.TEN,
                        null,
                        GoalPriority.LOW,
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Should reject invalid name")
    void shouldRejectInvalidName(String name) {
        assertThatThrownBy(() ->
                new CreateSavingsGoalCommand(
                        USER_ID,
                        name,
                        BigDecimal.TEN,
                        null,
                        GoalPriority.LOW,
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should reject name exceeding max length")
    void shouldRejectNameExceedingMaxLength() {
        assertThatThrownBy(() ->
                new CreateSavingsGoalCommand(
                        USER_ID,
                        "A".repeat(101),
                        BigDecimal.TEN,
                        null,
                        GoalPriority.LOW,
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.00", "-0.01"})
    @DisplayName("Should reject non-positive target amount")
    void shouldRejectNonPositiveTargetAmount(String amount) {
        assertThatThrownBy(() ->
                new CreateSavingsGoalCommand(
                        USER_ID,
                        "Goal",
                        new BigDecimal(amount),
                        null,
                        GoalPriority.LOW,
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should reject null target amount")
    void shouldRejectNullTargetAmount() {
        assertThatThrownBy(() ->
                new CreateSavingsGoalCommand(
                        USER_ID,
                        "Goal",
                        null,
                        null,
                        GoalPriority.LOW,
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should reject deadline not after today")
    void shouldRejectDeadlineNotAfterToday() {
        assertThatThrownBy(() ->
                new CreateSavingsGoalCommand(
                        USER_ID,
                        "Goal",
                        BigDecimal.TEN,
                        LocalDate.now(),
                        GoalPriority.LOW,
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }
}