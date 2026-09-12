package com.puntomartinez.millete.plannedtransactions.application.service;

import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.DeletePlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.ListPlannedTransactionsUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.ProcessScheduledTasksUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.RegisterPlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.UpdatePlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import com.puntomartinez.millete.transactions.domain.ports.in.RegisterTransactionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlannedTransactionService implements
        RegisterPlannedTransactionUseCase,
        ProcessScheduledTasksUseCase,
        UpdatePlannedTransactionUseCase,
        DeletePlannedTransactionUseCase,
        ListPlannedTransactionsUseCase {

    private final PlannedTransactionRepository plannedTransactionRepository;
    private final CategoryRepository categoryRepository;
    private final PlannedTransactionExecutionService executionService;

    @Override
    public PlannedTransaction register(
            RegisterPlannedTransactionCommand command
    ) {
        validateCategoryBelongsToUser(
                command.categoryId(),
                command.userId()
        );

        PlannedTransaction plannedTransaction =
                PlannedTransaction.create(
                        command.userId(),
                        command.categoryId(),
                        command.amount(),
                        command.type(),
                        command.description(),
                        command.frequencyType(),
                        command.frequencyInterval(),
                        command.startDate(),
                        command.endDate()
                );

        return plannedTransactionRepository.save(plannedTransaction);
    }

    @Override
    public synchronized void processScheduledTasks() {
        LocalDate today = LocalDate.now();

        List<PlannedTransaction> templates =
                plannedTransactionRepository.findAllActive();

        for (PlannedTransaction template : templates) {

            LocalDate pendingDate =
                    getNextPendingExecutionDate(
                            template,
                            today
                    );

            while (pendingDate != null) {

                try {
                    executionService.execute(
                            template,
                            pendingDate
                    );

                } catch (Exception e) {

                    log.error(
                            "Error ejecutando la transacción recurrente {} " +
                            "para la fecha {}. Se reintentará en la próxima " +
                            "ejecución del scheduler.",
                            template.getId(),
                            pendingDate,
                            e
                    );
                    break;
                }

                pendingDate =
                        getNextPendingExecutionDate(
                                template,
                                today
                        );
            }
        }
    }

    @Override
    public PlannedTransaction update(
            UUID id,
            UUID userId,
            UpdatePlannedTransactionCommand command
    ) {
        PlannedTransaction plannedTransaction =
                plannedTransactionRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Transacción recurrente no encontrada"
                                )
                        );

        if (!plannedTransaction.getUserId().equals(userId)) {
            throw new IllegalArgumentException(
                    "La transacción recurrente no pertenece al usuario"
            );
        }

        plannedTransaction.updateDetails(
                command.amount(),
                command.type(),
                command.description(),
                command.frequencyType(),
                command.frequencyInterval()
        );

        return plannedTransactionRepository.save(plannedTransaction);
    }

    @Override
    public void delete(
            UUID id,
            UUID userId
    ) {
        PlannedTransaction plannedTransaction =
                plannedTransactionRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Transacción recurrente no encontrada"
                                )
                        );

        if (!plannedTransaction.getUserId().equals(userId)) {
            throw new IllegalArgumentException(
                    "La transacción recurrente no pertenece al usuario"
            );
        }

        plannedTransaction.deactivate();

        plannedTransactionRepository.save(plannedTransaction);
    }

    @Override
    public List<PlannedTransaction> findAllByUserId(
            UUID userId
    ) {
        return plannedTransactionRepository.findAllByUserId(userId);
    }

    @Override
    public PlannedTransaction findByIdAndUserId(
            UUID id,
            UUID userId
    ) {
        PlannedTransaction plannedTransaction =
                plannedTransactionRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Transacción recurrente no encontrada"
                                )
                        );

        if (!plannedTransaction.getUserId().equals(userId)) {
            throw new IllegalArgumentException(
                    "La transacción recurrente no pertenece al usuario"
            );
        }

        return plannedTransaction;
    }

    private LocalDate getNextPendingExecutionDate(
            PlannedTransaction template,
            LocalDate today
    ) {
        LocalDate startDate = template.getStartDate();
        LocalDate endDate = template.getEndDate();
        LocalDate lastExecutedDate =
                template.getLastExecutedDate();

        if (startDate.isAfter(today)) {
            return null;
        }

        LocalDate nextExecution;

        if (lastExecutedDate == null) {
            nextExecution = startDate;
        } else {
            nextExecution = addFrequency(
                    lastExecutedDate,
                    template.getFrequencyType(),
                    template.getFrequencyInterval()
            );
        }

        if (nextExecution.isAfter(today)) {
            return null;
        }

        if (endDate != null &&
                nextExecution.isAfter(endDate)) {
            return null;
        }

        return nextExecution;
    }

    private LocalDate addFrequency(
            LocalDate baseDate,
            PlannedTransaction.FrequencyType frequencyType,
            Integer frequencyInterval
    ) {
        return switch (frequencyType) {
            case DAILY ->
                    baseDate.plusDays(frequencyInterval);

            case WEEKLY ->
                    baseDate.plusWeeks(frequencyInterval);

            case MONTHLY ->
                    baseDate.plusMonths(frequencyInterval);

            case YEARLY ->
                    baseDate.plusYears(frequencyInterval);
        };
    }

    private void validateCategoryBelongsToUser(
            UUID categoryId,
            UUID userId
    ) {
        if (categoryId == null) {
            throw new IllegalArgumentException(
                    "La categoría es obligatoria"
            );
        }

        boolean belongsToUser =
                categoryRepository.existsByIdAndUserId(
                        categoryId,
                        userId
                );

        if (!belongsToUser) {
            throw new IllegalArgumentException(
                    "La categoría no pertenece al usuario"
            );
        }
    }
}