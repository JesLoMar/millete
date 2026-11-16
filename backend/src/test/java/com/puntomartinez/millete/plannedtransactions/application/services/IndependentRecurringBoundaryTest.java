package com.puntomartinez.millete.plannedtransactions.application.services;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import com.puntomartinez.millete.users.application.services.UserLocalDateService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IndependentRecurringBoundaryTest {

    @Test
    void localTomorrowIsDueEvenWhenServerIsYesterday() throws Exception {
        assertEquals(
                List.of(LocalDate.of(2026, 2, 2)),
                run(
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 2, 2),
                        LocalDate.of(2026, 2, 2),
                        null,
                        null
                )
        );
    }

    @Test
    void finiteRecoveryStopsAtEndAndSecondRunDoesNotDuplicate() throws Exception {
        assertEquals(
                List.of(LocalDate.of(2026, 1, 30), LocalDate.of(2026, 1, 31)),
                run(
                        LocalDate.of(2026, 2, 2),
                        LocalDate.of(2026, 2, 2),
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 31),
                        LocalDate.of(2026, 1, 29)
                )
        );
    }

    private List<LocalDate> run(
            LocalDate serverDay,
            LocalDate localDay,
            LocalDate start,
            LocalDate end,
            LocalDate last
    ) throws Exception {
        UUID userId = UUID.randomUUID();
        PlannedTransaction template = mock(PlannedTransaction.class);
        when(template.getUserId()).thenReturn(userId);
        when(template.getStartDate()).thenReturn(start);
        when(template.getEndDate()).thenReturn(end);
        when(template.getFrequencyType()).thenReturn(PlannedTransaction.FrequencyType.DAYS);
        when(template.getFrequencyInterval()).thenReturn(1);
        AtomicReference<LocalDate> executed = new AtomicReference<>(last);
        when(template.getLastExecutedDate()).thenAnswer(invocation -> executed.get());

        PlannedTransactionRepository repository = mock(
                PlannedTransactionRepository.class,
                invocation -> invocation.getMethod().getName().equals("findAllActive")
                        ? List.of(template)
                        : RETURNS_DEFAULTS.answer(invocation)
        );
        TimeProvider clock = mock(TimeProvider.class);
        when(clock.localDateNow()).thenReturn(serverDay);
        UserLocalDateService localDates = mock(UserLocalDateService.class);
        when(localDates.todayFor(userId)).thenReturn(localDay);
        PlannedTransactionExecutionService execution = mock(PlannedTransactionExecutionService.class);
        List<LocalDate> calls = new ArrayList<>();
        doAnswer(invocation -> {
            LocalDate day = invocation.getArgument(1);
            calls.add(day);
            executed.set(day);
            return null;
        }).when(execution).execute(eq(template), any());

        Constructor<?> constructor = PlannedTransactionService.class.getConstructors()[0];
        PlannedTransactionService service = (PlannedTransactionService) (
                constructor.getParameterCount() == 4
                        ? constructor.newInstance(repository, execution, clock, localDates)
                        : constructor.newInstance(repository, execution, clock)
        );
        service.processScheduledTasks();
        service.processScheduledTasks();
        return calls;
    }
}
