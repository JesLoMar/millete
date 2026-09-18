package com.puntomartinez.millete.dashboard.infrastructure.out.savingsgoals;

import com.puntomartinez.millete.dashboard.domain.ports.out.SavingsGoalQueryPort;
import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.ports.out.SavingsGoalRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SavingsGoalQueryPostgresAdapter implements SavingsGoalQueryPort {

    private final SavingsGoalRepository savingsGoalRepository;

    public SavingsGoalQueryPostgresAdapter(
            SavingsGoalRepository savingsGoalRepository
    ) {
        this.savingsGoalRepository = savingsGoalRepository;
    }

    @Override
    public List<SavingsGoalData> findAllByUserId(UUID userId) {
        return savingsGoalRepository.findAllByUserId(userId)
                .stream()
                .map(this::toSavingsGoalData)
                .toList();
    }

    private SavingsGoalData toSavingsGoalData(SavingsGoal savingsGoal) {
        return new SavingsGoalData(
                savingsGoal.getId(),
                savingsGoal.getName(),
                savingsGoal.getTargetAmount(),
                savingsGoal.getCurrentAmount(),
                savingsGoal.getDeadline(),
                savingsGoal.getPriority() != null
                        ? savingsGoal.getPriority().name()
                        : null,
                savingsGoal.getCreatedAt()
        );
    }
}