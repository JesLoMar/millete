package com.puntomartinez.millete.dashboard.application.services;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

@Service
public class DashboardPeriodService {

    public LocalDateTime[] getDateRange(String period) {
        LocalDate today = LocalDate.now();

        return switch (period.toLowerCase()) {
            case "week" -> {
                LocalDate start = today.with(DayOfWeek.MONDAY);
                LocalDate end = start.plusDays(6);
                yield new LocalDateTime[]{
                        start.atStartOfDay(),
                        end.atTime(LocalTime.MAX)
                };
            }
            case "month" -> {
                LocalDate start = today.withDayOfMonth(1);
                LocalDate end = start.with(
                        TemporalAdjusters.lastDayOfMonth()
                );
                yield new LocalDateTime[]{
                        start.atStartOfDay(),
                        end.atTime(LocalTime.MAX)
                };
            }
            case "year" -> {
                LocalDate start = today.withDayOfYear(1);
                LocalDate end = start.with(
                        TemporalAdjusters.lastDayOfYear()
                );
                yield new LocalDateTime[]{
                        start.atStartOfDay(),
                        end.atTime(LocalTime.MAX)
                };
            }
            default -> throw new InvalidInputException(
                    "Periodo no válido: '" + period
                            + "'. Valores aceptados: week, month, year."
            );
        };
    }

    public LocalDateTime[] getPreviousPeriod(String period) {
        LocalDateTime[] currentRange = getDateRange(period);

        LocalDateTime previousStart = switch (period.toLowerCase()) {
            case "week" -> currentRange[0].minusWeeks(1);
            case "month" -> currentRange[0].minusMonths(1);
            case "year" -> currentRange[0].minusYears(1);
            default -> throw new InvalidInputException(
                    "Periodo no válido: '" + period
                            + "'. Valores aceptados: week, month, year."
            );
        };

        LocalDateTime previousEnd = currentRange[0].minusNanos(1);

        return new LocalDateTime[]{previousStart, previousEnd};
    }
}