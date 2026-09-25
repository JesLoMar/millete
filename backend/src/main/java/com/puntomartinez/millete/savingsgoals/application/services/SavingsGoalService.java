package com.puntomartinez.millete.savingsgoals.application.services;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.*;
import com.puntomartinez.millete.savingsgoals.domain.ports.out.SavingsGoalRepository;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SavingsGoalService implements
        CreateSavingsGoalUseCase,
        UpdateSavingsGoalUseCase,
        AddContributionToGoalUseCase,
        WithdrawFromGoalUseCase,
        ListSavingsGoalsUseCase,
        GetSavingsGoalUseCase,
        DeleteSavingsGoalUseCase {

    private final SavingsGoalRepository savingsGoalRepository;
    private final TimeProvider timeProvider;

    public SavingsGoalService(
            SavingsGoalRepository savingsGoalRepository,
            TimeProvider timeProvider
    ) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.timeProvider = timeProvider;
    }

    @Override
    @Transactional
    public SavingsGoal create(CreateSavingsGoalCommand command) {
        SavingsGoal goal = SavingsGoal.create(
                timeProvider,
                command.userId(),
                command.name(),
                command.targetAmount(),
                command.deadline(),
                command.priority(),
                command.link()
        );
        return savingsGoalRepository.save(goal);
    }

    @Override
    @Transactional
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

        goal.updateDetails(
                timeProvider,
                command.name(),
                command.targetAmount(),
                command.deadline(),
                command.priority(),
                command.link()
        );
        return savingsGoalRepository.save(goal);
    }

    @Override
    @Transactional
    public SavingsGoal addContribution(
            AddContributionToGoalCommand command
    ) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(
                        command.goalId(),
                        command.userId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Objetivo de ahorro no encontrado."
                        )
                );

        goal.addContribution(timeProvider, command.amount());
        return savingsGoalRepository.save(goal);
    }

    @Override
    @Transactional
    public SavingsGoal withdraw(
            WithdrawFromGoalCommand command
    ) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(
                        command.goalId(),
                        command.userId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Objetivo de ahorro no encontrado."
                        )
                );

        goal.withdraw(timeProvider, command.amount());
        return savingsGoalRepository.save(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsGoal> findByUserId(UUID userId) {
        return savingsGoalRepository.findAllByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsGoal> findByUserId(
            UUID userId,
            int page,
            int size,
            String search
    ) {
        return savingsGoalRepository.findAllByUserId(
                userId, page, size, search
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUserIdAndFilters(
            UUID userId,
            String search
    ) {
        return savingsGoalRepository.countByUserIdAndFilters(
                userId, search
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsGoal getByIdAndUserId(UUID id, UUID userId) {
        return savingsGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Objetivo de ahorro no encontrado."
                        )
                );
    }

    @Override
    @Transactional
    public void deleteByIdAndUserId(UUID id, UUID userId) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(
                        id, userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Objetivo de ahorro no encontrado."
                        )
                );

        goal.deactivate(timeProvider);
        savingsGoalRepository.save(goal);
    }
}
