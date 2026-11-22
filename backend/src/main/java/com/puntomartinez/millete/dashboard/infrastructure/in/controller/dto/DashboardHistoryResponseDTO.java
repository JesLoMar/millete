package com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DashboardHistoryResponseDTO(
        String period,
        List<String> labels,
        List<BigDecimal> data,
        List<Point> points
) {
    public DashboardHistoryResponseDTO(String period, List<String> labels, List<BigDecimal> data) {
        this(period, labels, data, List.of());
    }

    public record Point(LocalDate start, LocalDate end, BigDecimal income, BigDecimal expenses) {}
}
