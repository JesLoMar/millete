package com.puntomartinez.millete.plannedtransactions.application.services;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.DeletePlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.ListPlannedTransactionsUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.ProcessPlannedTransactionsUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.RegisterPlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.UpdatePlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlannedTransactionService implements
RegisterPlannedTransactionUseCase,
ProcessPlannedTransactionsUseCase,
UpdatePlannedTransactionUseCase,
DeletePlannedTransactionUseCase,
ListPlannedTransactionsUseCase {

private static final int SCHEDULER_BATCH_SIZE = 500;

private final PlannedTransactionRepository plannedTransactionRepository;
private final PlannedTransactionExecutionService executionService;

@Override
public PlannedTransaction register(
        RegisterPlannedTransactionCommand command
) {
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

    return plannedTransactionRepository.save(
            plannedTransaction
    );
}

@Override
public synchronized void processScheduledTasks() {
    LocalDate today = LocalDate.now();

    int page = 0;

    while (true) {
        List<PlannedTransaction> templates =
                plannedTransactionRepository.findAllActive(
                        page,
                        SCHEDULER_BATCH_SIZE
                );

        if (templates.isEmpty()) {
            break;
        }

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

        if (templates.size() < SCHEDULER_BATCH_SIZE) {
            break;
        }

        page++;
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

    validateOwnership(
            plannedTransaction,
            userId
    );

    plannedTransaction.updateDetails(
            command.amount(),
            command.type(),
            command.description(),
            command.frequencyType(),
            command.frequencyInterval()
    );

    return plannedTransactionRepository.save(
            plannedTransaction
    );
}

@Override
public void deleteByIdAndUserId(
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

    validateOwnership(
            plannedTransaction,
            userId
    );

    plannedTransaction.deactivate();

    plannedTransactionRepository.save(
            plannedTransaction
    );
}

@Override
public List<PlannedTransaction> findAllByUserId(
        UUID userId,
        int page,
        int size,
        String search,
        TransactionType type
) {
    return plannedTransactionRepository.findAllByUserId(
            userId,
            page,
            size,
            search,
            type
    );
}

@Override
public long countByUserIdAndFilters(
        UUID userId,
        String search,
        TransactionType type
) {
    return plannedTransactionRepository.countByUserIdAndFilters(
            userId,
            search,
            type
    );
}

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

    validateOwnership(
            plannedTransaction,
            userId
    );

    return plannedTransaction;
}

private LocalDate getNextPendingExecutionDate(
        PlannedTransaction template,
        LocalDate today
) {
    LocalDate startDate =
            template.getStartDate();

    LocalDate endDate =
            template.getEndDate();

    LocalDate lastExecutedDate =
            template.getLastExecutedDate();

    if (startDate.isAfter(today)) {
        return null;
    }

    LocalDate nextExecution;

    if (lastExecutedDate == null) {
        nextExecution = startDate;
    } else {
        nextExecution =
                addFrequency(
                        template,
                        lastExecutedDate
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
        PlannedTransaction template,
        LocalDate baseDate
) {
    PlannedTransaction.FrequencyType frequencyType =
            template.getFrequencyType();

    Integer frequencyInterval =
            template.getFrequencyInterval();

    LocalDate startDate =
            template.getStartDate();

    return switch (frequencyType) {

        case DAYS ->
                baseDate.plusDays(frequencyInterval);

        case WEEKS ->
                baseDate.plusWeeks(frequencyInterval);

        case MONTHS -> {
            YearMonth targetMonth =
                    YearMonth.from(baseDate)
                            .plusMonths(frequencyInterval);

            int dayOfMonth =
                    Math.min(
                            startDate.getDayOfMonth(),
                            targetMonth.lengthOfMonth()
                    );

            yield targetMonth.atDay(dayOfMonth);
        }

        case YEARS -> {
            int targetYear =
                    baseDate.getYear() + frequencyInterval;

            YearMonth targetMonth =
                    YearMonth.of(
                            targetYear,
                            startDate.getMonth()
                    );

            int dayOfMonth =
                    Math.min(
                            startDate.getDayOfMonth(),
                            targetMonth.lengthOfMonth()
                    );

            yield targetMonth.atDay(dayOfMonth);
        }
    };
}

private void validateOwnership(
        PlannedTransaction plannedTransaction,
        UUID userId
) {
    if (!plannedTransaction.getUserId().equals(userId)) {
        throw new IllegalArgumentException(
                "La transacción recurrente no pertenece al usuario"
        );
    }
}

}