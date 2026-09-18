package com.puntomartinez.millete.plannedtransactions.application.services;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.CategoryExistencePort;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.transactions.domain.ports.in.RegisterTransactionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlannedTransactionExecutionService {

    private static final String RECURRING_SUFFIX = " (Recurring)";
    private static final int MAX_DESCRIPTION_LENGTH = 50;

    private final RegisterTransactionUseCase registerTransactionUseCase;
    private final PlannedTransactionRepository plannedTransactionRepository;
    private final CategoryExistencePort categoryExistencePort;

    @Transactional
    public void execute(
            PlannedTransaction template,
            LocalDate executionDate
    ) {
        log.info(
                "Ejecutando transacción recurrente: {} para fecha {}",
                template.getId(),
                executionDate
        );

        UUID effectiveCategoryId =
                resolveCategoryId(template);

        RegisterTransactionUseCase.RegisterTransactionCommand command =
                new RegisterTransactionUseCase.RegisterTransactionCommand(
                        template.getUserId(),
                        effectiveCategoryId,
                        template.getAmount(),
                        executionDate.atStartOfDay(),
                        template.getType(),
                        buildRecurringDescription(
                                template.getDescription()
                        )
                );

        registerTransactionUseCase.register(command);

        template.markAsExecuted(executionDate);
        plannedTransactionRepository.save(template);
    }

    private UUID resolveCategoryId(PlannedTransaction template) {
        UUID categoryId = template.getCategoryId();

        if (categoryId == null) {
            return null;
        }

        boolean exists = categoryExistencePort.existsForUser(
                categoryId,
                template.getUserId()
        );

        if (!exists) {
            log.warn(
                    "La categoría {} de la plantilla recurrente {} "
                            + "ya no existe. Ejecutando sin categoría.",
                    categoryId,
                    template.getId()
            );
            return null;
        }

        return categoryId;
    }

    private String buildRecurringDescription(String description) {
        int maxBaseLength =
                MAX_DESCRIPTION_LENGTH - RECURRING_SUFFIX.length();

        String baseDescription =
                description.length() > maxBaseLength
                        ? description.substring(0, maxBaseLength)
                        : description;

        return baseDescription + RECURRING_SUFFIX;
    }
}