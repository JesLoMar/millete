package com.puntomartinez.millete.savingsgoals.domain.ports.in;

import com.puntomartinez.millete.savingsgoals.domain.utils.GoalPriority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UpdateSavingsGoalCommand")
class UpdateSavingsGoalCommandTest {

    private static final UUID GOAL_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    @DisplayName("Should create valid command with all fields")
    void shouldCreateValidCommandWithAllFields() {
        assertThatCode(() ->
                new UpdateSavingsGoalCommand(
                        GOAL_ID,
                        USER_ID,
                        "Updated",
                        BigDecimal.TEN,
                        LocalDate.now().plusDays(1),
                        GoalPriority.HIGH,
                        "https://example.com"
                )
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should allow null optional fields")
    void shouldAllowNullOptionalFields() {
        assertThatCode(() ->
                new UpdateSavingsGoalCommand(
                        GOAL_ID,
                        USER_ID,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject null id")
    void shouldRejectNullId() {
        assertThatThrownBy(() ->
                new UpdateSavingsGoalCommand(
                        null,
                        USER_ID,
                        "Name",
                        BigDecimal.TEN,
                        null,
                        null,
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should reject null user id")
    void shouldRejectNullUserId() {
        assertThatThrownBy(() ->
                new UpdateSavingsGoalCommand(
                        GOAL_ID,
                        null,
                        "Name",
                        BigDecimal.TEN,
                        null,
                        null,
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should reject name exceeding max length")
    void shouldRejectNameExceedingMaxLength() {
        assertThatThrownBy(() ->
                new UpdateSavingsGoalCommand(
                        GOAL_ID,
                        USER_ID,
                        "A".repeat(101),
                        null,
                        null,
                        null,
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"0.00", "-0.01"})
    @DisplayName("Should reject non-positive target amount")
    void shouldRejectNonPositiveTargetAmount(String amount) {
        assertThatThrownBy(() ->
                new UpdateSavingsGoalCommand(
                        GOAL_ID,
                        USER_ID,
                        null,
                        new BigDecimal(amount),
                        null,
                        null,
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should reject deadline not after today")
    void shouldRejectDeadlineNotAfterToday() {
        assertThatThrownBy(() ->
                new UpdateSavingsGoalCommand(
                        GOAL_ID,
                        USER_ID,
                        null,
                        null,
                        LocalDate.now(),
                        null,
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }
}