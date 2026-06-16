package com.puntomartinez.millete.savingsgoals.application.services;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.*;
import com.puntomartinez.millete.savingsgoals.domain.ports.out.SavingsGoalRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        SavingsGoal goal = new SavingsGoal();
        goal.setId(UUID.randomUUID());
        goal.setUserId(command.userId());
        goal.setName(command.name());
        goal.setTargetAmount(command.targetAmount());
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setDeadline(command.deadline());
        goal.setPriority(command.priority() != null ? command.priority() : "MEDIUM");
        goal.setStatus("ACTIVE");
        goal.setLink(command.link());
        goal.setCreatedAt(LocalDateTime.now());
        goal.setModifiedAt(LocalDateTime.now());
        goal.setActive(true);
        return savingsGoalRepository.save(goal);
    }

    @Override
    public SavingsGoal update(UpdateSavingsGoalCommand command) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(command.id(), command.userId())
                .orElseThrow(() -> new RuntimeException("Objetivo de ahorro no encontrado."));

        if (!goal.isActive()) {
            throw new RuntimeException("No se puede actualizar un objetivo inactivo.");
        }

        goal.updateDetails(
                command.name(),
                command.targetAmount(),
                command.deadline(),
                command.priority(),
                command.status(),
                command.link()
        );
        return savingsGoalRepository.save(goal);
    }

    @Override
    public SavingsGoal addContribution(AddContributionToGoalCommand command) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(command.goalId(), command.userId())
                .orElseThrow(() -> new RuntimeException("Objetivo de ahorro no encontrado."));

        if (!goal.isActive()) {
            throw new RuntimeException("No se puede contribuir a un objetivo inactivo.");
        }

        if (!"ACTIVE".equals(goal.getStatus())) {
            throw new RuntimeException("Solo se puede contribuir a objetivos en estado ACTIVE.");
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
    public List<SavingsGoal> findByUserIdAndStatus(UUID userId, String status) {
        return savingsGoalRepository.findAllByUserIdAndStatus(userId, status).stream()
                .filter(SavingsGoal::isActive)
                .toList();
    }

    @Override
    public SavingsGoal getByIdAndUserId(UUID id, UUID userId) {
        return savingsGoalRepository.findByIdAndUserId(id, userId)
                .filter(SavingsGoal::isActive)
                .orElseThrow(() -> new RuntimeException("Objetivo de ahorro no encontrado."));
    }

    @Override
    public void deleteByIdAndUserId(UUID id, UUID userId) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Objetivo de ahorro no encontrado."));

        if (!goal.isActive()) {
            throw new RuntimeException("El objetivo ya está inactivo.");
        }

        goal.deactivate();
        savingsGoalRepository.save(goal);
    }
}