package com.puntomartinez.millete.dashboard.application.services;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Service
public class DashboardPeriodService {

    private final TimeProvider timeProvider;

    public DashboardPeriodService(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public LocalDate[] getDateRange(String period) {
        LocalDate today = timeProvider.localDateNow();

        return switch (period.toLowerCase()) {
            case "week" -> {
                LocalDate start = today.with(DayOfWeek.MONDAY);
                LocalDate end = start.plusDays(6);
                yield new LocalDate[]{start, end};
            }
            case "month" -> {
                LocalDate start = today.withDayOfMonth(1);
                LocalDate end = start.with(
                        TemporalAdjusters.lastDayOfMonth()
                );
                yield new LocalDate[]{start, end};
            }
            case "year" -> {
                LocalDate start = today.withDayOfYear(1);
                LocalDate end = start.with(
                        TemporalAdjusters.lastDayOfYear()
                );
                yield new LocalDate[]{start, end};
            }
            default -> throw new InvalidInputException(
                    "Periodo no válido: '" + period
                            + "'. Valores aceptados: week, month, year."
            );
        };
    }

    public LocalDate[] getPreviousPeriod(String period) {
        LocalDate[] currentRange = getDateRange(period);

        LocalDate previousStart = switch (period.toLowerCase()) {
            case "week" -> currentRange[0].minusWeeks(1);
            case "month" -> currentRange[0].minusMonths(1);
            case "year" -> currentRange[0].minusYears(1);
            default -> throw new InvalidInputException(
                    "Periodo no válido: '" + period
                            + "'. Valores aceptados: week, month, year."
            );
        };

        LocalDate previousEnd = currentRange[0].minusDays(1);

        return new LocalDate[]{previousStart, previousEnd};
    }
}
