package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddSavingsGoalData(
        String name,
        BigDecimal targetAmount,
        SavingsGoalPriority priority,
        LocalDate deadline,
        String link
) implements InterpretationData {
}