package com.puntomartinez.millete.dashboard.application.services;

import com.puntomartinez.millete.dashboard.domain.ports.out.TransactionQueryPort;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.DashboardHistoryResponseDTO;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.users.application.services.UserLocalDateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de histórico de gastos del dashboard.
 *
 * <p>LÓGICA DE PERIODOS (deuda técnica documentada):
 * Este servicio tiene su propia lógica de rangos semanales/mensuales/anuales
 * en vez de reutilizar {@link DashboardPeriodService}. La razón histórica
 * es que el histórico necesita rangos más granulares (día a día, semana a
 * semana, mes a mes) que el servicio de periodos genérico no cubre.
 * Se podría valorar unificar criterios en un futuro refactor.</p>
 *
 * <p>HISTÓRICO MENSUAL: El periodo "month" no usa semanas naturales ni ISO,
 * sino bloques de 7 días desde el día 1 del mes. Esto significa que:</p>
 * <ul>
 *   <li>"Sem 1" = días 1-7</li>
 *   <li>"Sem 2" = días 8-14</li>
 *   <li>"Sem 3" = días 15-21</li>
 *   <li>"Sem 4" = días 22-28</li>
 *   <li>"Sem 5" = días 29-fin de mes (si aplica)</li>
 * </ul>
 * <p>Este comportamiento es intencional y debe mantenerse consistente
 * con lo que el frontend renderiza en los gráficos.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardHistoryService {

    private final TransactionQueryPort transactionQueryPort;
    private final UserLocalDateService userLocalDateService;

    public DashboardHistoryResponseDTO getHistory(
            java.util.UUID userId,
            String period
    ) {
        return switch (period.toLowerCase()) {
            case "week" -> getWeeklyHistory(userId);
            case "month" -> getMonthlyHistory(userId);
            case "year" -> getYearlyHistory(userId);
            default -> throw new InvalidInputException(
                    "Periodo no válido: '" + period
                            + "'. Valores aceptados: week, month, year."
            );
        };
    }

    private DashboardHistoryResponseDTO getWeeklyHistory(
            java.util.UUID userId
    ) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        String[] dayNames = {
                "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"
        };

        LocalDate today = userLocalDateService.todayFor(userId);
        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);

        List<TransactionQueryPort.TransactionData> transactions =
                transactionQueryPort.findByUserIdAndDateBetween(
                        userId,
                        weekStart,
                        weekStart.plusDays(6)
                );

        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            labels.add(dayNames[i]);

            if (day.isAfter(today)) {
                data.add(BigDecimal.ZERO);
                continue;
            }

            BigDecimal dayExpenses = transactions.stream()
                    .filter(t ->
                            t.date().equals(day))
                    .filter(t -> "EXPENSE".equals(t.type()))
                    .map(t -> t.amount().abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            data.add(dayExpenses);
        }

        return new DashboardHistoryResponseDTO("week", labels, data);
    }

    private DashboardHistoryResponseDTO getMonthlyHistory(
            java.util.UUID userId
    ) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        LocalDate today = userLocalDateService.todayFor(userId);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = monthStart.with(
                java.time.temporal.TemporalAdjusters.lastDayOfMonth()
        );

        List<TransactionQueryPort.TransactionData> transactions =
                transactionQueryPort.findByUserIdAndDateBetween(
                        userId,
                        monthStart,
                        lastDayOfMonth
                );

        int weekNumber = 1;
        LocalDate weekStart = monthStart;

        while (weekStart.isBefore(lastDayOfMonth.plusDays(1))
                && weekNumber <= 5) {

            LocalDate weekEnd = weekStart.plusDays(6);
            if (weekEnd.isAfter(lastDayOfMonth)) {
                weekEnd = lastDayOfMonth;
            }
            LocalDate finalWeekStart = weekStart;
            LocalDate finalWeekEnd = weekEnd;

            BigDecimal weekExpenses = transactions.stream()
                    .filter(t ->
                            !t.date().isBefore(finalWeekStart)
                                    && !t.date().isAfter(finalWeekEnd))
                    .filter(t -> "EXPENSE".equals(t.type()))
                    .map(t -> t.amount().abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            labels.add("Sem " + weekNumber);
            data.add(weekExpenses);

            weekStart = weekStart.plusWeeks(1);
            weekNumber++;
        }

        return new DashboardHistoryResponseDTO("month", labels, data);
    }

    private DashboardHistoryResponseDTO getYearlyHistory(
            java.util.UUID userId
    ) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        String[] monthNames = {
                "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
        };

        LocalDate today = userLocalDateService.todayFor(userId);
        int currentYear = today.getYear();

        List<TransactionQueryPort.TransactionData> transactions =
                transactionQueryPort.findByUserIdAndDateBetween(
                        userId,
                        LocalDate.of(currentYear, 1, 1),
                        today
                );

        for (int month = 1; month <= 12; month++) {
            LocalDate monthStart = LocalDate.of(currentYear, month, 1);

            if (monthStart.isAfter(today)) {
                break;
            }

            LocalDate monthEnd = monthStart.with(
                    java.time.temporal.TemporalAdjusters.lastDayOfMonth()
            );
            if (monthEnd.isAfter(today)) {
                monthEnd = today;
            }
            LocalDate finalMonthEnd = monthEnd;

            BigDecimal monthExpenses = transactions.stream()
                    .filter(t ->
                            !t.date().isBefore(monthStart)
                                    && !t.date().isAfter(finalMonthEnd))
                    .filter(t -> "EXPENSE".equals(t.type()))
                    .map(t -> t.amount().abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            labels.add(monthNames[month - 1]);
            data.add(monthExpenses);
        }

        return new DashboardHistoryResponseDTO("year", labels, data);
    }
}
