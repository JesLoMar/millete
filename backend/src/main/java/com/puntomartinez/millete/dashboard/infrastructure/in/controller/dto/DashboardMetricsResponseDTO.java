package com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto;

import java.math.BigDecimal;

/**
 * Métricas del dashboard para el periodo seleccionado.
 *
 * <p>CAMPOS REDUNDANTES (mantenidos por compatibilidad con frontend):</p>
 * <ul>
 *   <li>{@code savings} es idéntico a {@code balance} (income - expenses).
 *       Se mantiene como alias semántico para el frontend.</li>
 *   <li>{@code savingsTrend} es idéntico a {@code balanceTrend}.
 *       Se calcula con los mismos valores.</li>
 * </ul>
 * <p>Estos campos se eliminarán cuando se revise el frontend y se
 * pueda hacer la migración de forma coordinada.</p>
 */
public record DashboardMetricsResponseDTO(
        BigDecimal balance,
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal savings,
        double balanceTrend,
        double incomeTrend,
        double expensesTrend,
        double savingsTrend
) {
}