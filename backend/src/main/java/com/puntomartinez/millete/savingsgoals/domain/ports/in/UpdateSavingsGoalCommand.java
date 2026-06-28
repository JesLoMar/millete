package com.puntomartinez.millete.savingsgoals.domain.ports.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateSavingsGoalCommand(
        UUID id,
        UUID userId,
        String name,
        BigDecimal targetAmount,
        LocalDate deadline,
        String priority,
        String status,
        String link
) {
    public UpdateSavingsGoalCommand {
        if (id == null) throw new IllegalArgumentException("id es obligatorio.");
        if (userId == null) throw new IllegalArgumentException("userId es obligatorio.");
        if (status != null && !status.matches("^(ACTIVE|PAUSED|COMPLETED|CANCELLED)$"))
            throw new IllegalArgumentException("status inválido.");
    }
}