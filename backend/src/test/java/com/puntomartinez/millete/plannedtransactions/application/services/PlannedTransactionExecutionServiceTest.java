package com.puntomartinez.millete.plannedtransactions.application.services;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction.FrequencyType;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.CategoryExistencePort;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import com.puntomartinez.millete.transactions.domain.ports.in.RegisterTransactionUseCase;
import com.puntomartinez.millete.transactions.domain.ports.in.RegisterTransactionUseCase.RegisterTransactionCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlannedTransactionExecutionService")
class PlannedTransactionExecutionServiceTest {
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();

    @Mock
    private RegisterTransactionUseCase registerTransactionUseCase;

    @Mock
    private PlannedTransactionRepository plannedTransactionRepository;

    @Mock
    private CategoryExistencePort categoryExistencePort;

    @InjectMocks
    private PlannedTransactionExecutionService executionService;

    private PlannedTransaction buildTemplate(UUID categoryId, String description) {
        return PlannedTransaction.create(
                USER_ID, categoryId, new BigDecimal("100.00"),
                TransactionType.EXPENSE, description,
                FrequencyType.MONTHS, 1,
                LocalDate.now().minusMonths(1), null
        );
    }

    @Nested
    @DisplayName("execute")
    class Execute {
        @Test
        @DisplayName("Should register transaction and mark template as executed with valid category")
        void shouldRegisterAndMarkExecuted() {
            PlannedTransaction template = buildTemplate(CATEGORY_ID, "Rent");
            LocalDate executionDate = LocalDate.now();
            when(categoryExistencePort.existsForUser(CATEGORY_ID, USER_ID))
                    .thenReturn(true);
            when(plannedTransactionRepository.save(any()))
                    .thenAnswer(inv -> inv.getArgument(0));

            executionService.execute(template, executionDate);

            ArgumentCaptor<RegisterTransactionCommand> commandCaptor =
                    ArgumentCaptor.forClass(RegisterTransactionCommand.class);
            verify(registerTransactionUseCase).register(commandCaptor.capture());
            RegisterTransactionCommand command = commandCaptor.getValue();
            assertThat(command.userId()).isEqualTo(USER_ID);
            assertThat(command.categoryId()).isEqualTo(CATEGORY_ID);
            assertThat(command.amount()).isEqualByComparingTo("100.00");
            assertThat(command.description()).contains("Rent");
            assertThat(command.description()).contains("(Recurring)");

            assertThat(template.getLastExecutedDate()).isEqualTo(executionDate);
            verify(plannedTransactionRepository).save(template);
        }

        @Test
        @DisplayName("Should pass null category when template has no category id")
        void shouldPassNullCategoryWhenNoCategoryId() {
            PlannedTransaction template = buildTemplate(null, "Expense");
            LocalDate executionDate = LocalDate.now();

            executionService.execute(template, executionDate);

            ArgumentCaptor<RegisterTransactionCommand> captor =
                    ArgumentCaptor.forClass(RegisterTransactionCommand.class);
            verify(registerTransactionUseCase).register(captor.capture());
            assertThat(captor.getValue().categoryId()).isNull();
            verify(categoryExistencePort, never()).existsForUser(any(), any());
        }

        @Test
        @DisplayName("Should pass null category when category no longer exists")
        void shouldPassNullCategoryWhenCategoryMissing() {
            PlannedTransaction template = buildTemplate(CATEGORY_ID, "Rent");
            LocalDate executionDate = LocalDate.now();
            when(categoryExistencePort.existsForUser(CATEGORY_ID, USER_ID))
                    .thenReturn(false);

            executionService.execute(template, executionDate);

            ArgumentCaptor<RegisterTransactionCommand> captor =
                    ArgumentCaptor.forClass(RegisterTransactionCommand.class);
            verify(registerTransactionUseCase).register(captor.capture());
            assertThat(captor.getValue().categoryId()).isNull();
        }

        @Test
        @DisplayName("Should truncate long descriptions and append recurring suffix")
        void shouldTruncateLongDescriptions() {
            String longDescription = "A".repeat(50);
            PlannedTransaction template = buildTemplate(CATEGORY_ID, longDescription);
            LocalDate executionDate = LocalDate.now();
            when(categoryExistencePort.existsForUser(CATEGORY_ID, USER_ID))
                    .thenReturn(true);

            executionService.execute(template, executionDate);

            ArgumentCaptor<RegisterTransactionCommand> captor =
                    ArgumentCaptor.forClass(RegisterTransactionCommand.class);
            verify(registerTransactionUseCase).register(captor.capture());
            String generatedDesc = captor.getValue().description();
            assertThat(generatedDesc).endsWith("(Recurring)");
            assertThat(generatedDesc.length()).isLessThanOrEqualTo(50);
        }

        @Test
        @DisplayName("Should use full base description when short enough")
        void shouldKeepFullDescriptionWhenShort() {
            PlannedTransaction template = buildTemplate(CATEGORY_ID, "Rent");
            LocalDate executionDate = LocalDate.now();
            when(categoryExistencePort.existsForUser(CATEGORY_ID, USER_ID))
                    .thenReturn(true);

            executionService.execute(template, executionDate);

            ArgumentCaptor<RegisterTransactionCommand> captor =
                    ArgumentCaptor.forClass(RegisterTransactionCommand.class);
            verify(registerTransactionUseCase).register(captor.capture());
            assertThat(captor.getValue().description())
                    .isEqualTo("Rent (Recurring)");
        }
    }
}