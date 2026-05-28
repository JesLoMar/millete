package com.puntomartinez.millete.shared.infrastructure.config.scheduler;

import com.puntomartinez.millete.plannedtransactions.domain.ports.in.ProcessPlannedTransactionsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TransactionScheduler {

    private final ProcessPlannedTransactionsUseCase processUseCase;

    public TransactionScheduler(ProcessPlannedTransactionsUseCase processUseCase) {
        this.processUseCase = processUseCase;
    }

    // Se ejecuta todos los días a las 00:01 AM
    @Scheduled(cron = "0 1 0 * * ?")
    public void runDailyTransactions() {
        processUseCase.processScheduledTasks();
    }
}