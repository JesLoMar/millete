package com.puntomartinez.millete.dataexport.domain.model;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.puntomartinez.millete.shared.infrastructure.config.JacksonConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LegacySavingsGoalSnapshotTest {

    private static final String LEGACY_EXPORT = """
            {
              "metadata": {
                "version": "0.1.0",
                "appVersion": "0.1.4",
                "exportDate": "2025-01-01T12:00:00"
              },
              "savingsGoals": [{
                "name": "Test goal",
                "targetAmount": 250.00,
                "currentAmount": 75.00,
                "active": true,
                "%s": "ACTIVE"
              }]
            }
            """;

    @Test
    void ignoresLegacyGoalStatusAndKeepsGoalData() throws Exception {
        var mapper = new JacksonConfig().objectMapper();
        var snapshot = mapper.readValue(LEGACY_EXPORT.formatted("status"), UserDataSnapshot.class);

        assertThat(snapshot.savingsGoals()).singleElement().satisfies(goal -> {
            assertThat(goal.name()).isEqualTo("Test goal");
            assertThat(goal.targetAmount()).isEqualByComparingTo("250.00");
            assertThat(goal.currentAmount()).isEqualByComparingTo("75.00");
            assertThat(goal.active()).isTrue();
        });
        assertThat(mapper.valueToTree(snapshot).path("savingsGoals").get(0).has("status")).isFalse();
    }

    @Test
    void stillRejectsOtherUnknownGoalFields() {
        var mapper = new JacksonConfig().objectMapper();

        assertThatThrownBy(() ->
                mapper.readValue(LEGACY_EXPORT.formatted("unknownField"), UserDataSnapshot.class))
                .isInstanceOfSatisfying(UnrecognizedPropertyException.class,
                        error -> assertThat(error.getPropertyName()).isEqualTo("unknownField"));
    }
}
