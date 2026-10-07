package com.puntomartinez.millete.assistant.domain.ports.out;

import com.puntomartinez.millete.assistant.domain.model.interpretation.SavingsGoalResolution;

import java.util.UUID;

public interface SavingsGoalResolver {

    SavingsGoalResolution resolve(
            UUID userId,
            String goalReference
    );
}