package com.puntomartinez.millete.plannedtransactions.application.services;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.transactions.domain.ports.in.RegisterTransactionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlannedTransactionExecutionService {

    private static final String RECURRING_SUFFIX = " (Recurring)";
    private static final int MAX_DESCRIPTION_LENGTH = 50;

    private final RegisterTransactionUseCase registerTransactionUseCase;
    private final PlannedTransactionRepository plannedTransactionRepository;

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

        RegisterTransactionUseCase.RegisterTransactionCommand command =
                new RegisterTransactionUseCase.RegisterTransactionCommand(
                        template.getUserId(),
                        template.getCategoryId(),
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