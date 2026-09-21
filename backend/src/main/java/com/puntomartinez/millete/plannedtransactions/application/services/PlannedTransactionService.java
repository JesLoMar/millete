package com.puntomartinez.millete.plannedtransactions.application.services;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.DeletePlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.GetPlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.ListPlannedTransactionsUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.ProcessPlannedTransactionsUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.RegisterPlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.UpdatePlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
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
        ProcessPlannedTransactionsUseCase,
        UpdatePlannedTransactionUseCase,
        DeletePlannedTransactionUseCase,
        ListPlannedTransactionsUseCase,
        GetPlannedTransactionUseCase {

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

    /**
     * Procesa todas las plantillas activas que tengan ejecuciones
     * pendientes.
     *
     * Nota: el modificador 'synchronized' protege contra ejecuciones
     * concurrentes dentro de la misma JVM. Para despliegues
     * multi-instancia se necesitaría un lock distribuido (ShedLock,
     * líder único, etc.). En el contexto self-hosted single-instance
     * actual, 'synchronized' es suficiente.
     */
    @Override
    public synchronized void processScheduledTasks() {
        LocalDate today = LocalDate.now();
        int page = 0;

        while (true) {
            List<PlannedTransaction> templates =
                    plannedTransactionRepository.findAllActive(
                            page,
                            SCHEDULER_BATCH_SIZE,
                            today
                    );

            if (templates.isEmpty()) {
                break;
            }

            for (PlannedTransaction template : templates) {
                processTemplate(template, today);
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
                getActiveTemplate(id, userId);

        plannedTransaction.updateDetails(
                command.amount(),
                command.type(),
                command.description(),
                command.frequencyType(),
                command.frequencyInterval(),
                command.categoryId()
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
                getActiveTemplate(id, userId);

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

    @Override
    public PlannedTransaction getByIdAndUserId(
            UUID id,
            UUID userId
    ) {
        return getActiveTemplate(id, userId);
    }

    private void processTemplate(
            PlannedTransaction template,
            LocalDate today
    ) {
        LocalDate pendingDate =
                getNextPendingExecutionDate(template, today);

        while (pendingDate != null) {
            try {
                executionService.execute(template, pendingDate);
            } catch (Exception e) {
                boolean shouldDeactivate =
                        template.incrementFailureCount();

                if (shouldDeactivate) {
                    template.deactivate();
                    log.warn(
                            "La plantilla recurrente {} se ha "
                                    + "desactivado tras {} fallos "
                                    + "consecutivos. Motivo: {}",
                            template.getId(),
                            PlannedTransaction.MAX_CONSECUTIVE_FAILURES,
                            e.getMessage() // 👈 Cambiado de 'e' a 'e.getMessage()'
                    );
                } else {
                    log.error(
                            "Error ejecutando la plantilla recurrente "
                                    + "{} para la fecha {} "
                                    + "(intento {}/{}). Motivo: {}",
                            template.getId(),
                            pendingDate,
                            template.getFailureCount(),
                            PlannedTransaction.MAX_CONSECUTIVE_FAILURES,
                            e.getMessage() // 👈 Cambiado de 'e' a 'e.getMessage()'
                    );
                }
                plannedTransactionRepository.save(template);
                break;
            }
            pendingDate =
                    getNextPendingExecutionDate(template, today);
        }
    }

    private LocalDate getNextPendingExecutionDate(
            PlannedTransaction template,
            LocalDate today
    ) {
        LocalDate startDate = template.getStartDate();
        LocalDate endDate = template.getEndDate();
        LocalDate lastExecutedDate = template.getLastExecutedDate();

        if (startDate.isAfter(today)) {
            return null;
        }

        LocalDate nextExecution;

        if (lastExecutedDate == null) {
            nextExecution = startDate;
        } else {
            nextExecution = addFrequency(template, lastExecutedDate);
        }

        if (nextExecution.isAfter(today)) {
            return null;
        }

        if (endDate != null && nextExecution.isAfter(endDate)) {
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
        LocalDate startDate = template.getStartDate();

        return switch (frequencyType) {
            case DAYS ->
                    baseDate.plusDays(frequencyInterval);
            case WEEKS ->
                    baseDate.plusWeeks(frequencyInterval);
            case MONTHS -> {
                java.time.YearMonth targetMonth =
                        java.time.YearMonth.from(baseDate)
                                .plusMonths(frequencyInterval);
                int dayOfMonth = Math.min(
                        startDate.getDayOfMonth(),
                        targetMonth.lengthOfMonth()
                );
                yield targetMonth.atDay(dayOfMonth);
            }
            case YEARS -> {
                int targetYear =
                        baseDate.getYear() + frequencyInterval;
                java.time.YearMonth targetMonth =
                        java.time.YearMonth.of(
                                targetYear,
                                startDate.getMonth()
                        );
                int dayOfMonth = Math.min(
                        startDate.getDayOfMonth(),
                        targetMonth.lengthOfMonth()
                );
                yield targetMonth.atDay(dayOfMonth);
            }
        };
    }

    private PlannedTransaction getActiveTemplate(
            UUID id,
            UUID userId
    ) {
        PlannedTransaction plannedTransaction =
                plannedTransactionRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Transacción recurrente no encontrada"
                                )
                        );

        if (!plannedTransaction.getUserId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "La transacción recurrente no pertenece al usuario"
            );
        }

        if (!plannedTransaction.isActive()) {
            throw new ResourceNotFoundException(
                    "Transacción recurrente no encontrada"
            );
        }

        return plannedTransaction;
    }
}