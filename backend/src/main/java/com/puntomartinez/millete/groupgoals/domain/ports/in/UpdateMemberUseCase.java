package com.puntomartinez.millete.groupgoals.domain.ports.in;

import com.puntomartinez.millete.groupgoals.domain.model.GoalRole;

import java.math.BigDecimal;
import java.util.UUID;

public interface UpdateMemberUseCase {

    void updateMember(
            UUID goalId,
            UUID memberId,
            UUID userId,
            UpdateMemberCommand command
    );

    record UpdateMemberCommand(
            GoalRole role,
            BigDecimal salary,
            BigDecimal customPercentage
    ) {
    }
}