package com.puntomartinez.millete.dataexport.infrastructure.out.savingsgoals;

import com.puntomartinez.millete.dataexport.domain.model.SavingsGoalSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.SavingsGoalImportPort;
import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.ports.out.SavingsGoalRepository;
import com.puntomartinez.millete.savingsgoals.domain.utils.GoalPriority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SavingsGoalImportAdapter
        implements SavingsGoalImportPort {

    private final SavingsGoalRepository savingsGoalRepository;

    public SavingsGoalImportAdapter(
            SavingsGoalRepository savingsGoalRepository
    ) {
        this.savingsGoalRepository = savingsGoalRepository;
    }

    @Override
    public int importSavingsGoals(
            List<SavingsGoalSnapshot> savingsGoals,
            UUID userId
    ) {
        if (savingsGoals == null || savingsGoals.isEmpty()) {
            return 0;
        }

        int count = 0;

        for (SavingsGoalSnapshot savingsGoal : savingsGoals) {
            if (!savingsGoal.active()) {
                continue;
            }

            GoalPriority priority = savingsGoal.priority() != null
                    ? GoalPriority.valueOf(savingsGoal.priority())
                    : GoalPriority.MEDIUM;

            SavingsGoal importedSavingsGoal =
                    SavingsGoal.reconstitute(
                            UUID.randomUUID(),
                            userId,
                            savingsGoal.name(),
                            savingsGoal.targetAmount(),
                            savingsGoal.currentAmount(),
                            savingsGoal.deadline(),
                            priority,
                            savingsGoal.link(),
                            savingsGoal.createdAt(),
                            savingsGoal.modifiedAt(),
                            savingsGoal.active()
                    );

            savingsGoalRepository.save(
                    importedSavingsGoal
            );
            count++;
        }

        return count;
    }
}