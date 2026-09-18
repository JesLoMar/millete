package com.puntomartinez.millete.transactions.application.services;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

@Service
public class TransactionPeriodService {

    public LocalDateTime[] getDateRange(String period) {
        LocalDateTime now = LocalDateTime.now();

        return switch (period.toLowerCase()) {
            case "week" -> new LocalDateTime[]{
                    now.with(DayOfWeek.MONDAY)
                            .withHour(0)
                            .withMinute(0)
                            .withSecond(0)
                            .withNano(0),
                    now.with(DayOfWeek.SUNDAY)
                            .with(LocalTime.MAX)
            };
            case "month" -> new LocalDateTime[]{
                    now.withDayOfMonth(1)
                            .withHour(0)
                            .withMinute(0)
                            .withSecond(0)
                            .withNano(0),
                    now.with(TemporalAdjusters.lastDayOfMonth())
                            .with(LocalTime.MAX)
            };
            case "year" -> new LocalDateTime[]{
                    now.withDayOfYear(1)
                            .withHour(0)
                            .withMinute(0)
                            .withSecond(0)
                            .withNano(0),
                    now.with(TemporalAdjusters.lastDayOfYear())
                            .with(LocalTime.MAX)
            };
            default -> throw new InvalidInputException(
                    "Periodo no válido: " + period
            );
        };
    }

    public LocalDateTime[] getPreviousPeriod(String period) {
        LocalDateTime[] currentRange = getDateRange(period);

        LocalDateTime previousStart;
        LocalDateTime previousEnd;

        switch (period.toLowerCase()) {
            case "week" -> {
                previousStart = currentRange[0].minusWeeks(1);
                previousEnd = currentRange[1].minusWeeks(1);
            }
            case "month" -> {
                previousStart = currentRange[0].minusMonths(1);
                previousEnd = currentRange[0]
                        .minusDays(1)
                        .with(LocalTime.MAX);
            }
            case "year" -> {
                previousStart = currentRange[0].minusYears(1);
                previousEnd = currentRange[0]
                        .minusDays(1)
                        .with(LocalTime.MAX);
            }
            default -> throw new InvalidInputException(
                    "Periodo no válido: " + period
            );
        }

        return new LocalDateTime[]{previousStart, previousEnd};
    }
}