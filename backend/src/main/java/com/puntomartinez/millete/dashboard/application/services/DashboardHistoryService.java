package com.puntomartinez.millete.dashboard.application.services;

import com.puntomartinez.millete.dashboard.domain.ports.out.TransactionQueryPort;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.DashboardHistoryResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardHistoryService {

    private final TransactionQueryPort transactionQueryPort;

    public DashboardHistoryResponseDTO getHistory(
            java.util.UUID userId,
            String period
    ) {
        return switch (period.toLowerCase()) {
            case "week" -> getWeeklyHistory(userId);
            case "month" -> getMonthlyHistory(userId);
            case "year" -> getYearlyHistory(userId);
            default -> throw new IllegalArgumentException(
                    "Invalid period: " + period
            );
        };
    }

    private DashboardHistoryResponseDTO getWeeklyHistory(
            java.util.UUID userId
    ) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        String[] dayNames = {
                "Lun",
                "Mar",
                "Mié",
                "Jue",
                "Vie",
                "Sáb",
                "Dom"
        };

        LocalDate today = LocalDate.now();

        LocalDate weekStart =
                today.with(java.time.DayOfWeek.MONDAY);

        List<TransactionQueryPort.TransactionData> transactions =
                transactionQueryPort.findByUserIdAndDateBetween(
                        userId,
                        weekStart.atStartOfDay(),
                        weekStart
                                .plusDays(6)
                                .atTime(LocalTime.MAX)
                );

        for (int i = 0; i < 7; i++) {
            LocalDate day =
                    weekStart.plusDays(i);

            labels.add(dayNames[i]);

            if (day.isAfter(today)) {
                data.add(BigDecimal.ZERO);
                continue;
            }

            LocalDateTime dayStart =
                    day.atStartOfDay();

            LocalDateTime dayEnd =
                    day.atTime(LocalTime.MAX);

            BigDecimal dayExpenses =
                    transactions.stream()
                            .filter(t ->
                                    !t.date().isBefore(dayStart)
                                            && !t.date().isAfter(dayEnd))
                            .filter(t ->
                                    "EXPENSE".equals(t.type()))
                            .map(t ->
                                    t.amount().abs())
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            data.add(dayExpenses);
        }

        return new DashboardHistoryResponseDTO(
                "week",
                labels,
                data
        );
    }

    private DashboardHistoryResponseDTO getMonthlyHistory(
            java.util.UUID userId
    ) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        LocalDate today = LocalDate.now();

        LocalDate monthStart =
                today.withDayOfMonth(1);

        LocalDate lastDayOfMonth =
                monthStart.with(
                        java.time.temporal.TemporalAdjusters.lastDayOfMonth()
                );

        List<TransactionQueryPort.TransactionData> transactions =
                transactionQueryPort.findByUserIdAndDateBetween(
                        userId,
                        monthStart.atStartOfDay(),
                        lastDayOfMonth.atTime(LocalTime.MAX)
                );

        int weekNumber = 1;
        LocalDate weekStart = monthStart;

        while (
                weekStart.isBefore(
                        lastDayOfMonth.plusDays(1)
                )
                        && weekNumber <= 5
        ) {
            LocalDate weekEnd =
                    weekStart.plusDays(6);

            if (weekEnd.isAfter(lastDayOfMonth)) {
                weekEnd = lastDayOfMonth;
            }

            LocalDateTime startDateTime =
                    weekStart.atStartOfDay();

            LocalDateTime endDateTime =
                    weekEnd.atTime(LocalTime.MAX);

            BigDecimal weekExpenses =
                    transactions.stream()
                            .filter(t ->
                                    !t.date().isBefore(startDateTime)
                                            && !t.date().isAfter(endDateTime))
                            .filter(t ->
                                    "EXPENSE".equals(t.type()))
                            .map(t ->
                                    t.amount().abs())
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            labels.add("Sem " + weekNumber);
            data.add(weekExpenses);

            weekStart =
                    weekStart.plusWeeks(1);

            weekNumber++;
        }

        return new DashboardHistoryResponseDTO(
                "month",
                labels,
                data
        );
    }

    private DashboardHistoryResponseDTO getYearlyHistory(
            java.util.UUID userId
    ) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        String[] monthNames = {
                "Ene",
                "Feb",
                "Mar",
                "Abr",
                "May",
                "Jun",
                "Jul",
                "Ago",
                "Sep",
                "Oct",
                "Nov",
                "Dic"
        };

        LocalDate today = LocalDate.now();

        int currentYear =
                today.getYear();

        List<TransactionQueryPort.TransactionData> transactions =
                transactionQueryPort.findByUserIdAndDateBetween(
                        userId,
                        LocalDate.of(
                                currentYear,
                                1,
                                1
                        ).atStartOfDay(),
                        today.atTime(LocalTime.MAX)
                );

        for (int month = 1; month <= 12; month++) {
            LocalDate monthStart =
                    LocalDate.of(
                            currentYear,
                            month,
                            1
                    );

            if (monthStart.isAfter(today)) {
                break;
            }

            LocalDate monthEnd =
                    monthStart.with(
                            java.time.temporal.TemporalAdjusters.lastDayOfMonth()
                    );

            if (monthEnd.isAfter(today)) {
                monthEnd = today;
            }

            LocalDateTime startDateTime =
                    monthStart.atStartOfDay();

            LocalDateTime endDateTime =
                    monthEnd.atTime(LocalTime.MAX);

            BigDecimal monthExpenses =
                    transactions.stream()
                            .filter(t ->
                                    !t.date().isBefore(startDateTime)
                                            && !t.date().isAfter(endDateTime))
                            .filter(t ->
                                    "EXPENSE".equals(t.type()))
                            .map(t ->
                                    t.amount().abs())
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            labels.add(monthNames[month - 1]);
            data.add(monthExpenses);
        }

        return new DashboardHistoryResponseDTO(
                "year",
                labels,
                data
        );
    }
}