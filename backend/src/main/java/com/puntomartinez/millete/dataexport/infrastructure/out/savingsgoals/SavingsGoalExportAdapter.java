package com.puntomartinez.millete.dataexport.infrastructure.out.savingsgoals;

import com.puntomartinez.millete.dataexport.domain.model.SavingsGoalSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.SavingsGoalExportPort;
import com.puntomartinez.millete.savingsgoals.domain.ports.out.SavingsGoalRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SavingsGoalExportAdapter implements SavingsGoalExportPort {

    private final SavingsGoalRepository savingsGoalRepository;

    public SavingsGoalExportAdapter(
            SavingsGoalRepository savingsGoalRepository
    ) {
        this.savingsGoalRepository = savingsGoalRepository;
    }

    @Override
    public List<SavingsGoalSnapshot> findAllByUserId(UUID userId) {
        return savingsGoalRepository.findAllByUserId(userId)
                .stream()
                .map(savingsGoal ->
                        new SavingsGoalSnapshot(
                                savingsGoal.getId(),
                                savingsGoal.getUserId(),
                                savingsGoal.getName(),
                                savingsGoal.getTargetAmount(),
                                savingsGoal.getCurrentAmount(),
                                savingsGoal.getDeadline(),
                                savingsGoal.getPriority() != null
                                        ? savingsGoal.getPriority().name()
                                        : null,
                                savingsGoal.getLink(),
                                savingsGoal.getCreatedAt(),
                                savingsGoal.getModifiedAt(),
                                savingsGoal.isActive()
                        )
                )
                .toList();
    }
}