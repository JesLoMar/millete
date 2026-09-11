package com.puntomartinez.millete.savingsgoals.application.services;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.*;
import com.puntomartinez.millete.savingsgoals.domain.ports.out.SavingsGoalRepository;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SavingsGoalService implements
CreateSavingsGoalUseCase,
UpdateSavingsGoalUseCase,
AddContributionToGoalUseCase,
ListSavingsGoalsUseCase,
GetSavingsGoalUseCase,
DeleteSavingsGoalUseCase {

private final SavingsGoalRepository savingsGoalRepository;

public SavingsGoalService(SavingsGoalRepository savingsGoalRepository) {
    this.savingsGoalRepository = savingsGoalRepository;
}

@Override
public SavingsGoal create(CreateSavingsGoalCommand command) {
    SavingsGoal goal = SavingsGoal.create(
            command.userId(),
            command.name(),
            command.targetAmount(),
            command.deadline(),
            command.priority(),
            LinkSanitizer.normalizeAndValidate(command.link())
    );

    return savingsGoalRepository.save(goal);
}

@Override
public SavingsGoal update(UpdateSavingsGoalCommand command) {
    SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(
                    command.id(),
                    command.userId()
            )
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Objetivo de ahorro no encontrado."
                    )
            );

    if (!goal.isActive()) {
        throw new InvalidInputException(
                "No se puede actualizar un objetivo inactivo."
        );
    }

    goal.updateDetails(
            command.name(),
            command.targetAmount(),
            command.deadline(),
            command.priority(),
            LinkSanitizer.normalizeAndValidate(command.link())
    );

    return savingsGoalRepository.save(goal);
}

@Override
public SavingsGoal addContribution(AddContributionToGoalCommand command) {
    SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(
                    command.goalId(),
                    command.userId()
            )
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Objetivo de ahorro no encontrado."
                    )
            );

    if (!goal.isActive()) {
        throw new InvalidInputException(
                "No se puede contribuir a un objetivo inactivo."
        );
    }

    goal.addContribution(command.amount());

    return savingsGoalRepository.save(goal);
}

@Override
public List<SavingsGoal> findByUserId(UUID userId) {
    return savingsGoalRepository.findAllByUserId(userId).stream()
            .filter(SavingsGoal::isActive)
            .toList();
}

@Override
public List<SavingsGoal> findByUserId(
        UUID userId,
        int page,
        int size,
        String search
) {
    return savingsGoalRepository.findAllByUserId(
            userId,
            page,
            size,
            search
    );
}

@Override
public long countByUserIdAndFilters(
        UUID userId,
        String search
) {
    return savingsGoalRepository.countByUserIdAndFilters(
            userId,
            search
    );
}

@Override
public SavingsGoal getByIdAndUserId(UUID id, UUID userId) {
    return savingsGoalRepository.findByIdAndUserId(id, userId)
            .filter(SavingsGoal::isActive)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Objetivo de ahorro no encontrado."
                    )
            );
}

@Override
public void deleteByIdAndUserId(UUID id, UUID userId) {
    SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(
                    id,
                    userId
            )
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Objetivo de ahorro no encontrado."
                    )
            );

    if (!goal.isActive()) {
        throw new InvalidInputException(
                "El objetivo ya está inactivo."
        );
    }

    goal.deactivate();
    savingsGoalRepository.save(goal);
}
}