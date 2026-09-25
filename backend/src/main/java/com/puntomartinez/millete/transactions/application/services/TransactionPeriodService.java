package com.puntomartinez.millete.transactions.application.services;

import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Service
public class TransactionPeriodService {

    private final TimeProvider timeProvider;

    public TransactionPeriodService(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public LocalDate[] getDateRange(String period) {
        LocalDate today = timeProvider.localDateNow();

        return switch (period.toLowerCase()) {
            case "week" -> {
                LocalDate start = today.with(DayOfWeek.MONDAY);
                yield new LocalDate[]{start, start.plusDays(6)};
            }
            case "month" -> {
                LocalDate start = today.withDayOfMonth(1);
                yield new LocalDate[]{
                        start,
                        start.with(TemporalAdjusters.lastDayOfMonth())
                };
            }
            case "year" -> {
                LocalDate start = today.withDayOfYear(1);
                yield new LocalDate[]{
                        start,
                        start.with(TemporalAdjusters.lastDayOfYear())
                };
            }
            default -> throw new InvalidInputException(
                    "Periodo no válido: " + period
            );
        };
    }

    public LocalDate[] getPreviousPeriod(String period) {
        LocalDate[] currentRange = getDateRange(period);

        LocalDate previousStart;
        LocalDate previousEnd;

        switch (period.toLowerCase()) {
            case "week" -> {
                previousStart = currentRange[0].minusWeeks(1);
                previousEnd = currentRange[1].minusWeeks(1);
            }
            case "month" -> {
                previousStart = currentRange[0].minusMonths(1);
                previousEnd = currentRange[0].minusDays(1);
            }
            case "year" -> {
                previousStart = currentRange[0].minusYears(1);
                previousEnd = currentRange[0].minusDays(1);
            }
            default -> throw new InvalidInputException(
                    "Periodo no válido: " + period
            );
        }

        return new LocalDate[]{previousStart, previousEnd};
    }
}