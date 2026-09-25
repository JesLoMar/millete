package com.puntomartinez.millete.dashboard.application.services;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import com.puntomartinez.millete.shared.domain.time.FixedTimeProvider;
import java.time.Instant;

@DisplayName("DashboardPeriodService")
class DashboardPeriodServiceTest {
    static final TimeProvider TIME = new FixedTimeProvider(
            Instant.parse("2024-01-15T10:00:00Z"));


    private DashboardPeriodService service;

    @BeforeEach
    void setUp() {
        service = new DashboardPeriodService(TIME);
    }

    @Nested
    @DisplayName("getDateRange")
    class GetDateRange {

        @Test
        @DisplayName("Should return week range from Monday to Sunday")
        void shouldReturnWeekRange() {
            LocalDate today = LocalDate.now();
            LocalDate expectedStart = today.with(DayOfWeek.MONDAY);
            LocalDate expectedEnd = expectedStart.plusDays(6);

            LocalDate[] result = service.getDateRange("week");

            assertThat(result[0]).isEqualTo(expectedStart.atStartOfDay());
            assertThat(result[1]).isEqualTo(expectedEnd.atTime(LocalTime.MAX));
        }

        @Test
        @DisplayName("Should return month range from first to last day")
        void shouldReturnMonthRange() {
            LocalDate today = LocalDate.now();
            LocalDate expectedStart = today.withDayOfMonth(1);
            LocalDate expectedEnd = expectedStart.with(
                    TemporalAdjusters.lastDayOfMonth()
            );

            LocalDate[] result = service.getDateRange("month");

            assertThat(result[0]).isEqualTo(expectedStart.atStartOfDay());
            assertThat(result[1]).isEqualTo(expectedEnd.atTime(LocalTime.MAX));
        }

        @Test
        @DisplayName("Should return year range from Jan 1 to Dec 31")
        void shouldReturnYearRange() {
            LocalDate today = LocalDate.now();
            LocalDate expectedStart = today.withDayOfYear(1);
            LocalDate expectedEnd = expectedStart.with(
                    TemporalAdjusters.lastDayOfYear()
            );

            LocalDate[] result = service.getDateRange("year");

            assertThat(result[0]).isEqualTo(expectedStart.atStartOfDay());
            assertThat(result[1]).isEqualTo(expectedEnd.atTime(LocalTime.MAX));
        }

        @ParameterizedTest
        @ValueSource(strings = {"WEEK", "Week", "MONTH", "Month", "YEAR", "Year"})
        @DisplayName("Should be case-insensitive")
        void shouldBeCaseInsensitive(String period) {
            LocalDate[] result = service.getDateRange(period);

            assertThat(result).hasSize(2);
            assertThat(result[0]).isNotNull();
            assertThat(result[1]).isNotNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"day", "quarter", "invalid", ""})
        @DisplayName("Should throw for invalid period")
        void shouldThrowForInvalidPeriod(String period) {
            assertThatThrownBy(() -> service.getDateRange(period))
                    .isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("getPreviousPeriod")
    class GetPreviousPeriod {

        @Test
        @DisplayName("Should return previous week range")
        void shouldReturnPreviousWeekRange() {
            LocalDateTime[] currentRange = service.getDateRange("week");
            LocalDate[] result = service.getPreviousPeriod("week");

            assertThat(result[0]).isEqualTo(currentRange[0].minusWeeks(1));
            assertThat(result[1]).isEqualTo(currentRange[0].minusNanos(1));
        }

        @Test
        @DisplayName("Should return previous month range")
        void shouldReturnPreviousMonthRange() {
            LocalDateTime[] currentRange = service.getDateRange("month");
            LocalDate[] result = service.getPreviousPeriod("month");

            assertThat(result[0]).isEqualTo(currentRange[0].minusMonths(1));
            assertThat(result[1]).isEqualTo(currentRange[0].minusNanos(1));
        }

        @Test
        @DisplayName("Should return previous year range")
        void shouldReturnPreviousYearRange() {
            LocalDateTime[] currentRange = service.getDateRange("year");
            LocalDate[] result = service.getPreviousPeriod("year");

            assertThat(result[0]).isEqualTo(currentRange[0].minusYears(1));
            assertThat(result[1]).isEqualTo(currentRange[0].minusNanos(1));
        }

        @ParameterizedTest
        @ValueSource(strings = {"day", "quarter", "invalid"})
        @DisplayName("Should throw for invalid period")
        void shouldThrowForInvalidPeriod(String period) {
            assertThatThrownBy(() -> service.getPreviousPeriod(period))
                    .isInstanceOf(InvalidInputException.class);
        }
    }
}