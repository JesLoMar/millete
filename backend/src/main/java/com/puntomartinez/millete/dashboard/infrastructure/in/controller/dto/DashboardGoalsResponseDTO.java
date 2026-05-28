package com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto;

import java.util.List;

public record DashboardGoalsResponseDTO(
        List<SavingsGoalResponseDTO> goals
) {}