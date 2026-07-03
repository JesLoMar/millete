package com.puntomartinez.millete.savingsgoals.domain.ports.in;

import java.util.UUID;

public interface DeleteSavingsGoalUseCase {
    void deleteByIdAndUserId(UUID id, UUID userId);
}
