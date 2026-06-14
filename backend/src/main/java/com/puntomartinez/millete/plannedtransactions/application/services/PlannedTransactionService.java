package com.puntomartinez.millete.plannedtransactions.application.services;

import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction.FrequencyType;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.DeletePlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.ProcessPlannedTransactionsUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.RegisterPlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.UpdatePlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.transactions.domain.ports.in.RegisterTransactionUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PlannedTransactionService implements
        RegisterPlannedTransactionUseCase,
        ProcessPlannedTransactionsUseCase,
        UpdatePlannedTransactionUseCase,
        DeletePlannedTransactionUseCase {

    private final PlannedTransactionRepository plannedTransactionRepository;
    private final CategoryRepository categoryRepository;
    private final RegisterTransactionUseCase registerTransactionUseCase;

    public PlannedTransactionService(PlannedTransactionRepository plannedTransactionRepository,
                                     CategoryRepository categoryRepository,
                                     RegisterTransactionUseCase registerTransactionUseCase) {
        this.plannedTransactionRepository = plannedTransactionRepository;
        this.categoryRepository = categoryRepository;
        this.registerTransactionUseCase = registerTransactionUseCase;
    }

    // =======================================================
    // REGISTRAR PLANTILLA
    // =======================================================
    
    @Override
    public PlannedTransaction register(RegisterPlannedTransactionCommand command) {
        if (command.categoryId() != null) {
            categoryRepository.findById(command.categoryId())
                    .orElseThrow(() -> new RuntimeException("The specified category does not exist."));
        }

        UUID newId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        PlannedTransaction newPlannedTransaction = new PlannedTransaction(
                newId,
                command.userId(),
                command.categoryId(),
                command.amount(),
                command.type(),
                command.description(),
                command.frequencyType(),
                command.frequencyInterval(),
                command.startDate(),
                command.endDate(),
                now,
                now,
                true,
                null
        );

        return plannedTransactionRepository.save(newPlannedTransaction);
    }

    // =======================================================
    // PROCESAR PLANTILLAS (Catch-up Mode)
    // =======================================================

    @Override
    @Transactional
    public void processScheduledTasks() {
        LocalDate today = LocalDate.now();

        List<PlannedTransaction> templates = plannedTransactionRepository.findAllActive();

        for (PlannedTransaction template : templates) {
            LocalDate pendingDate = getNextPendingExecutionDate(template, today);

            while (pendingDate != null) {
                log.info("Executing recurring transaction: {} ({}€) for user {} scheduled for {}",
                        template.getDescription(), template.getAmount(), template.getUserId(), pendingDate);

                RegisterTransactionUseCase.RegisterTransactionCommand command =
                        new RegisterTransactionUseCase.RegisterTransactionCommand(
                                template.getUserId(),
                                template.getCategoryId(),
                                template.getAmount(),
                                pendingDate.atStartOfDay(),
                                template.getType(),
                                template.getDescription() + " (Recurring)"
                        );

                registerTransactionUseCase.register(command);

                template.setLastExecutedDate(pendingDate);
                plannedTransactionRepository.save(template);

                pendingDate = getNextPendingExecutionDate(template, today);
            }
        }
    }

    // =======================================================
    // ACTUALIZAR PLANTILLA
    // =======================================================

    @Override
    public PlannedTransaction update(UUID id, UpdatePlannedTransactionUseCase.UpdatePlannedTransactionCommand command) {
        PlannedTransaction tx = plannedTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Planned transaction not found."));

        if (!tx.getUserId().equals(command.userId())) {
            throw new RuntimeException("You do not have permission to edit this transaction.");
        }

        if (command.categoryId() != null) {
            categoryRepository.findById(command.categoryId())
                    .orElseThrow(() -> new RuntimeException("Category does not exist."));
        }

        boolean recurrenceChanged = !tx.getStartDate().equals(command.startDate()) ||
                                    tx.getFrequencyType() != command.frequencyType() ||
                                    !tx.getFrequencyInterval().equals(command.frequencyInterval());

        tx.setAmount(command.amount());
        tx.setType(command.type());
        tx.setDescription(command.description());
        tx.setCategoryId(command.categoryId());
        tx.setFrequencyType(command.frequencyType());
        tx.setFrequencyInterval(command.frequencyInterval());
        tx.setStartDate(command.startDate());
        tx.setEndDate(command.endDate());
        tx.setModifiedAt(LocalDateTime.now());

        if (recurrenceChanged) {
            tx.setLastExecutedDate(null);
        }

        return plannedTransactionRepository.save(tx);
    }

    // =======================================================
    // ELIMINAR PLANTILLA (SOFT DELETE)
    // =======================================================

    @Override
    public void deleteByIdAndUserId(UUID id, UUID userId) {
        PlannedTransaction tx = plannedTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Planned transaction not found."));

        if (!tx.getUserId().equals(userId)) {
            throw new RuntimeException("You do not have permission to delete this transaction.");
        }

        tx.setActive(false);
        tx.setModifiedAt(LocalDateTime.now());
        plannedTransactionRepository.save(tx);
    }

    public List<PlannedTransaction> findByUserId(UUID userId) {
        return plannedTransactionRepository.findAllByUserId(userId).stream()
                .filter(PlannedTransaction::isActive)
                .toList();
    }

    // =======================================================
    // LÓGICA DE RECURRENCIA
    // =======================================================

    private LocalDate getNextPendingExecutionDate(PlannedTransaction template, LocalDate today) {
        LocalDate start = template.getStartDate();
        LocalDate end = template.getEndDate();

        if (start.isAfter(today)) {
            return null;
        }

        LocalDate baseDate = template.getLastExecutedDate();
        LocalDate nextExecution = start;

        if (baseDate != null) {
            long periodsToBase = calculatePeriodsPassed(start, baseDate, template.getFrequencyType(), template.getFrequencyInterval());
            nextExecution = addPeriods(start, periodsToBase, template.getFrequencyType(), template.getFrequencyInterval());

            while (!nextExecution.isAfter(baseDate)) {
                periodsToBase++;
                nextExecution = addPeriods(start, periodsToBase, template.getFrequencyType(), template.getFrequencyInterval());
            }
        }

        if (!nextExecution.isAfter(today) && (end == null || !nextExecution.isAfter(end))) {
            return nextExecution;
        }

        return null;
    }

    private long calculatePeriodsPassed(LocalDate start, LocalDate target, FrequencyType type, int interval) {
        return switch (type) {
            case DAYS -> ChronoUnit.DAYS.between(start, target) / interval;
            case WEEKS -> ChronoUnit.WEEKS.between(start, target) / interval;
            case MONTHS -> ChronoUnit.MONTHS.between(start, target) / interval;
            case YEARS -> ChronoUnit.YEARS.between(start, target) / interval;
        };
    }

    private LocalDate addPeriods(LocalDate start, long periods, FrequencyType type, int interval) {
        long totalUnits = periods * interval;
        return switch (type) {
            case DAYS -> start.plusDays(totalUnits);
            case WEEKS -> start.plusWeeks(totalUnits);
            case MONTHS -> start.plusMonths(totalUnits);
            case YEARS -> start.plusYears(totalUnits);
        };
    }
}