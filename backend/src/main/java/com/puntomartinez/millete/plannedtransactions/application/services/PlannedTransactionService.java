package com.puntomartinez.millete.plannedtransactions.application.services;

import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.DeletePlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.ProcessPlannedTransactionsUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.RegisterPlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.UpdatePlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.transactions.domain.ports.in.RegisterTransactionUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PlannedTransactionService implements
        RegisterPlannedTransactionUseCase,
        ProcessPlannedTransactionsUseCase,
        UpdatePlannedTransactionUseCase,
        DeletePlannedTransactionUseCase {

    private final PlannedTransactionRepository plannedTransactionRepository;
    private final CategoryRepository categoryRepository;
    private final RegisterTransactionUseCase registerTransactionUseCase;

    // Inyectamos el caso de uso de las Transacciones Reales
    public PlannedTransactionService(PlannedTransactionRepository plannedTransactionRepository,
                                     CategoryRepository categoryRepository,
                                     RegisterTransactionUseCase registerTransactionUseCase) {
        this.plannedTransactionRepository = plannedTransactionRepository;
        this.categoryRepository = categoryRepository;
        this.registerTransactionUseCase = registerTransactionUseCase;
    }

    // =======================================================
    // REGISTRAR PLANTILLA (Historia 2 - Acción del Usuario)
    // =======================================================
    @Override
    public PlannedTransaction register(RegisterPlannedTransactionCommand command) {
        if (command.categoryId() != null) {
            categoryRepository.findById(command.categoryId())
                    .orElseThrow(() -> new RuntimeException("La categoría especificada no existe."));
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
                true
        );

        return plannedTransactionRepository.save(newPlannedTransaction);
    }

    // =======================================================
    // PROCESAR PLANTILLAS (Historia 2 - Acción Automática)
    // =======================================================
    @Override
    @Transactional
    public void processScheduledTasks() {
        LocalDate today = LocalDate.now();

        // 1. Buscamos todas las plantillas que están activas
        List<PlannedTransaction> templates = plannedTransactionRepository.findAllActive();

        for (PlannedTransaction template : templates) {
            if (shouldExecuteToday(template, today)) {
                // 2. Creamos el comando para el módulo de Transacciones
                RegisterTransactionUseCase.RegisterTransactionCommand command = new RegisterTransactionUseCase.RegisterTransactionCommand(
                        template.getUserId(),
                        template.getCategoryId(),
                        template.getAmount(),
                        today.atStartOfDay(), // Guardamos la fecha de hoy a las 00:00
                        template.getType(),
                        template.getDescription() + " (Recurrente)" // Añadimos una marca visual
                );

                // 3. Ejecutamos el registro real
                registerTransactionUseCase.register(command);
            }
        }
    }

    // =======================================================
// ACTUALIZAR PLANTILLA
// =======================================================
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

        tx.setAmount(command.amount());
        tx.setType(command.type());
        tx.setDescription(command.description());
        tx.setCategoryId(command.categoryId());
        tx.setFrequencyType(command.frequencyType());
        tx.setFrequencyInterval(command.frequencyInterval());
        tx.setStartDate(command.startDate());
        tx.setEndDate(command.endDate());
        tx.setModifiedAt(LocalDateTime.now());

        return plannedTransactionRepository.save(tx);
    }

    // =======================================================
// ELIMINAR PLANTILLA (SOFT DELETE)
// =======================================================
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

    // Lógica para saber si toca cobrar/pagar hoy
    private boolean shouldExecuteToday(PlannedTransaction template, LocalDate today) {
        // De momento, comprobamos si hoy es el día exacto de inicio.
        // (En el futuro aquí pondremos la matemática de los intervalos)
        return template.getStartDate().equals(today);
    }
}