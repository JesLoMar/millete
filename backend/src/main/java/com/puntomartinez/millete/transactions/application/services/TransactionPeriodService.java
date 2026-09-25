package com.puntomartinez.millete.transactions.application.services;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
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
        LocalDate now = timeProvider.localDateNow();

        return switch (period.toLowerCase()) {
            case "week" -> new LocalDate[]{
                    now.with(DayOfWeek.MONDAY),
                    now.with(DayOfWeek.SUNDAY)
            };
            case "month" -> new LocalDate[]{
                    now.withDayOfMonth(1),
                    now.with(TemporalAdjusters.lastDayOfMonth())
            };
            case "year" -> new LocalDate[]{
                    now.withDayOfYear(1),
                    now.with(TemporalAdjusters.lastDayOfYear())
            };
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
