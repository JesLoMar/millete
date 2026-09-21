package com.puntomartinez.millete.savingsgoals.domain.ports.in;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AddContributionToGoalCommand")
class AddContributionToGoalCommandTest {

    private static final UUID GOAL_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    @DisplayName("Should create valid command")
    void shouldCreateValidCommand() {
        assertThatCode(() ->
                new AddContributionToGoalCommand(GOAL_ID, USER_ID, BigDecimal.TEN)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject null goal id")
    void shouldRejectNullGoalId() {
        assertThatThrownBy(() ->
                new AddContributionToGoalCommand(null, USER_ID, BigDecimal.TEN)
        ).isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("Should reject null user id")
    void shouldRejectNullUserId() {
        assertThatThrownBy(() ->
                new AddContributionToGoalCommand(GOAL_ID, null, BigDecimal.TEN)
        ).isInstanceOf(InvalidInputException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.00", "-0.01", "-100.00"})
    @DisplayName("Should reject non-positive amount")
    void shouldRejectNonPositiveAmount(String amount) {
        assertThatThrownBy(() ->
                new AddContributionToGoalCommand(GOAL_ID, USER_ID, new BigDecimal(amount))
        ).isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("Should reject null amount")
    void shouldRejectNullAmount() {
        assertThatThrownBy(() ->
                new AddContributionToGoalCommand(GOAL_ID, USER_ID, null)
        ).isInstanceOf(InvalidInputException.class);
    }
}