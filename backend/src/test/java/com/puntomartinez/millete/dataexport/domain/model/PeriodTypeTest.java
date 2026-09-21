package com.puntomartinez.millete.dataexport.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PeriodType")
class PeriodTypeTest {

    @Test
    @DisplayName("fromCode should return correct period")
    void fromCodeShouldReturnCorrectPeriod() {
        assertThat(PeriodType.fromCode("1m")).isEqualTo(PeriodType.ONE_MONTH);
        assertThat(PeriodType.fromCode("3m")).isEqualTo(PeriodType.THREE_MONTHS);
        assertThat(PeriodType.fromCode("6m")).isEqualTo(PeriodType.SIX_MONTHS);
        assertThat(PeriodType.fromCode("1y")).isEqualTo(PeriodType.ONE_YEAR);
    }

    @Test
    @DisplayName("fromCode should be case-insensitive")
    void fromCodeShouldBeCaseInsensitive() {
        assertThat(PeriodType.fromCode("1M")).isEqualTo(PeriodType.ONE_MONTH);
        assertThat(PeriodType.fromCode("1Y")).isEqualTo(PeriodType.ONE_YEAR);
    }

    @Test
    @DisplayName("fromCode should throw on invalid code")
    void fromCodeShouldThrowOnInvalidCode() {
        assertThatThrownBy(() -> PeriodType.fromCode("invalid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getStartDate should calculate correct date")
    void getStartDateShouldCalculateCorrectDate() {
        LocalDate today = LocalDate.now();

        assertThat(PeriodType.ONE_MONTH.getStartDate()).isEqualTo(today.minusMonths(1));
        assertThat(PeriodType.THREE_MONTHS.getStartDate()).isEqualTo(today.minusMonths(3));
        assertThat(PeriodType.SIX_MONTHS.getStartDate()).isEqualTo(today.minusMonths(6));
        assertThat(PeriodType.ONE_YEAR.getStartDate()).isEqualTo(today.minusMonths(12));
    }

    @Test
    @DisplayName("getEndDate should return today")
    void getEndDateShouldReturnToday() {
        assertThat(PeriodType.ONE_MONTH.getEndDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("getDisplayName should return readable name")
    void getDisplayNameShouldReturnReadableName() {
        assertThat(PeriodType.ONE_MONTH.getDisplayName()).isEqualTo("1 month");
        assertThat(PeriodType.THREE_MONTHS.getDisplayName()).isEqualTo("3 months");
        assertThat(PeriodType.SIX_MONTHS.getDisplayName()).isEqualTo("6 months");
        assertThat(PeriodType.ONE_YEAR.getDisplayName()).isEqualTo("1 year");
    }

    @Test
    @DisplayName("getCode should return correct code")
    void getCodeShouldReturnCorrectCode() {
        assertThat(PeriodType.ONE_MONTH.getCode()).isEqualTo("1m");
        assertThat(PeriodType.THREE_MONTHS.getCode()).isEqualTo("3m");
        assertThat(PeriodType.SIX_MONTHS.getCode()).isEqualTo("6m");
        assertThat(PeriodType.ONE_YEAR.getCode()).isEqualTo("1y");
    }

    @Test
    @DisplayName("getMonths should return correct months")
    void getMonthsShouldReturnCorrectMonths() {
        assertThat(PeriodType.ONE_MONTH.getMonths()).isEqualTo(1);
        assertThat(PeriodType.THREE_MONTHS.getMonths()).isEqualTo(3);
        assertThat(PeriodType.SIX_MONTHS.getMonths()).isEqualTo(6);
        assertThat(PeriodType.ONE_YEAR.getMonths()).isEqualTo(12);
    }
}