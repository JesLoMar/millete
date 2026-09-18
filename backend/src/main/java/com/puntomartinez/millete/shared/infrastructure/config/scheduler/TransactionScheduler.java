package com.puntomartinez.millete.shared.infrastructure.config.scheduler;

import com.puntomartinez.millete.plannedtransactions.domain.ports.in.ProcessPlannedTransactionsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler de transacciones recurrentes.
 *
 * <p>DEPENDENCIA CON 'plannedtransactions': Este scheduler depende de
 * ProcessPlannedTransactionsUseCase del módulo 'plannedtransactions'.
 * Para reducir el acoplamiento del kernel con módulos de negocio,
 * se podría migrar a un mecanismo de eventos o un registro de jobs
 * donde cada módulo registre sus propios schedulers.</p>
 *
 * <p>CRON: Se ejecuta diariamente a las 00:01. Si la aplicación se
 * internacionaliza, valorar procesamiento más frecuente o por zona
 * horaria.</p>
 */
@Component
public class TransactionScheduler {

    private final ProcessPlannedTransactionsUseCase processUseCase;

    public TransactionScheduler(ProcessPlannedTransactionsUseCase processUseCase) {
        this.processUseCase = processUseCase;
    }

    @Scheduled(cron = "0 1 0 * * ?")
    public void runDailyTransactions() {
        processUseCase.processScheduledTasks();
    }
}