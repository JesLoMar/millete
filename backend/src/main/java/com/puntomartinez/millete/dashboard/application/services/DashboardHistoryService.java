package com.puntomartinez.millete.dashboard.application.services;

import com.puntomartinez.millete.dashboard.domain.ports.out.TransactionQueryPort;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.DashboardHistoryResponseDTO;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.DashboardHistoryResponseDTO.Point;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.users.application.services.UserLocalDateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Histórico de ingresos y gastos hasta el día local del usuario. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardHistoryService {

    private static final String[] DAYS = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
    private static final String[] MONTHS = {
            "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    };

    private final TransactionQueryPort transactionQueryPort;
    private final UserLocalDateService userLocalDateService;

    public DashboardHistoryResponseDTO getHistory(UUID userId, String period) {
        String normalized = period.toLowerCase(Locale.ROOT);
        Window window = switch (normalized) {
            case "week" -> new Window(7, ChronoUnit.DAYS);
            case "month" -> new Window(5, ChronoUnit.WEEKS);
            case "year" -> new Window(12, ChronoUnit.MONTHS);
            default -> throw new InvalidInputException(
                    "Periodo no válido: '" + period + "'. Valores aceptados: week, month, year."
            );
        };

        LocalDate today = userLocalDateService.todayFor(userId);
        LocalDate start = window.bucketStart(today).minus(window.count() - 1L, window.unit());
        Map<LocalDate, BigDecimal> income = new HashMap<>();
        Map<LocalDate, BigDecimal> expenses = new HashMap<>();
        for (var transaction : transactionQueryPort.findByUserIdAndDateBetween(userId, start, today)) {
            LocalDate day = transaction.date();
            if (day.isBefore(start) || day.isAfter(today)) {
                continue;
            }

            LocalDate bucket = window.bucketStart(day);
            switch (transaction.type()) {
                case "INCOME" -> income.merge(bucket, transaction.amount().abs(), BigDecimal::add);
                case "EXPENSE" -> expenses.merge(bucket, transaction.amount().abs(), BigDecimal::add);
                default -> { }
            }
        }

        List<Point> points = new ArrayList<>(window.count());
        List<String> labels = new ArrayList<>(window.count());
        for (int i = 0; i < window.count(); i++) {
            LocalDate from = start.plus(i, window.unit());
            LocalDate to = i == window.count() - 1 ? today : from.plus(1, window.unit()).minusDays(1);
            points.add(new Point(from, to, income.getOrDefault(from, BigDecimal.ZERO),
                    expenses.getOrDefault(from, BigDecimal.ZERO)));
            labels.add(switch (window.unit()) {
                case DAYS -> DAYS[from.getDayOfWeek().ordinal()];
                case WEEKS -> "Sem " + (i + 1);
                case MONTHS -> MONTHS[from.getMonthValue() - 1];
                default -> throw new IllegalStateException("Unidad de histórico no válida");
            });
        }

        return new DashboardHistoryResponseDTO(normalized, labels, points.stream().map(Point::expenses).toList(), points);
    }

    private record Window(int count, ChronoUnit unit) {
        LocalDate bucketStart(LocalDate date) {
            return switch (unit) {
                case DAYS -> date;
                case WEEKS -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                case MONTHS -> date.withDayOfMonth(1);
                default -> throw new IllegalStateException("Unidad de histórico no válida");
            };
        }
    }
}
