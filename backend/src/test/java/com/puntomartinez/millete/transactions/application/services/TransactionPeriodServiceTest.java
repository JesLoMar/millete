package com.puntomartinez.millete.transactions.application.services;

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

@DisplayName("TransactionPeriodService")
class TransactionPeriodServiceTest {

    private TransactionPeriodService service;

    @BeforeEach
    void setUp() {
        service = new TransactionPeriodService();
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

            LocalDateTime[] result = service.getDateRange("week");

            assertThat(result[0]).isEqualTo(expectedStart.atStartOfDay());
            assertThat(result[1]).isEqualTo(expectedEnd.atTime(LocalTime.MAX));
        }

        @Test
        @DisplayName("Should return month range from first to last day")
        void shouldReturnMonthRange() {
            LocalDate today = LocalDate.now();
            LocalDate expectedStart = today.withDayOfMonth(1);
            LocalDate expectedEnd = expectedStart.with(TemporalAdjusters.lastDayOfMonth());

            LocalDateTime[] result = service.getDateRange("month");

            assertThat(result[0]).isEqualTo(expectedStart.atStartOfDay());
            assertThat(result[1]).isEqualTo(expectedEnd.atTime(LocalTime.MAX));
        }

        @Test
        @DisplayName("Should return year range from Jan 1 to Dec 31")
        void shouldReturnYearRange() {
            LocalDate today = LocalDate.now();
            LocalDate expectedStart = today.withDayOfYear(1);
            LocalDate expectedEnd = expectedStart.with(TemporalAdjusters.lastDayOfYear());

            LocalDateTime[] result = service.getDateRange("year");

            assertThat(result[0]).isEqualTo(expectedStart.atStartOfDay());
            assertThat(result[1]).isEqualTo(expectedEnd.atTime(LocalTime.MAX));
        }

        @ParameterizedTest
        @ValueSource(strings = {"WEEK", "Week", "MONTH", "Month", "YEAR", "Year"})
        @DisplayName("Should be case-insensitive")
        void shouldBeCaseInsensitive(String period) {
            LocalDateTime[] result = service.getDateRange(period);

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
            LocalDateTime[] result = service.getPreviousPeriod("week");

            assertThat(result[0]).isEqualTo(currentRange[0].minusWeeks(1));
            assertThat(result[1]).isEqualTo(currentRange[1].minusWeeks(1));
        }

        @Test
        @DisplayName("Should return previous month range ending at last microsecond before current month")
        void shouldReturnPreviousMonthRange() {
            LocalDateTime[] currentRange = service.getDateRange("month");
            LocalDateTime[] result = service.getPreviousPeriod("month");

            assertThat(result[0]).isEqualTo(currentRange[0].minusMonths(1));
            assertThat(result[1]).isEqualTo(currentRange[0].minusDays(1).with(LocalTime.MAX));
        }

        @Test
        @DisplayName("Should return previous year range ending at last microsecond before current year")
        void shouldReturnPreviousYearRange() {
            LocalDateTime[] currentRange = service.getDateRange("year");
            LocalDateTime[] result = service.getPreviousPeriod("year");

            assertThat(result[0]).isEqualTo(currentRange[0].minusYears(1));
            assertThat(result[1]).isEqualTo(currentRange[0].minusDays(1).with(LocalTime.MAX));
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