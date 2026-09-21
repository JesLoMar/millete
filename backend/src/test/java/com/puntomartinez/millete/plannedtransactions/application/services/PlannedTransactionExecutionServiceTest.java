package com.puntomartinez.millete.plannedtransactions.application.services;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction.FrequencyType;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.CategoryExistencePort;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import com.puntomartinez.millete.transactions.domain.ports.in.RegisterTransactionUseCase;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
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
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private PlannedTransaction createTemplate() {
        return PlannedTransaction.create(
                USER_ID, CATEGORY_ID, new BigDecimal("100.00"),
                TransactionType.EXPENSE, "Rent",
                FrequencyType.MONTHS, 1,
                LocalDate.now().minusMonths(1), null
        );
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("Should register transaction and mark template as executed")
        void shouldRegisterAndMarkAsExecuted() {
            PlannedTransaction template = createTemplate();
            LocalDate executionDate = LocalDate.now();

            when(categoryExistencePort.existsForUser(CATEGORY_ID, USER_ID))
                    .thenReturn(true);
            when(registerTransactionUseCase.register(any()))
                    .thenAnswer(invocation -> {
                        var cmd = (RegisterTransactionUseCase.RegisterTransactionCommand)
                                invocation.getArgument(0);
                        return Transaction.create(
                                cmd.userId(), cmd.categoryId(), cmd.amount(),
                                cmd.date(), cmd.type(), cmd.description()
                        );
                    });
            when(plannedTransactionRepository.save(template)).thenReturn(template);

            executionService.execute(template, executionDate);

            ArgumentCaptor<RegisterTransactionUseCase.RegisterTransactionCommand> captor =
                    ArgumentCaptor.forClass(RegisterTransactionUseCase.RegisterTransactionCommand.class);
            verify(registerTransactionUseCase).register(captor.capture());

            RegisterTransactionUseCase.RegisterTransactionCommand command = captor.getValue();

            assertThat(command.userId()).isEqualTo(USER_ID);
            assertThat(command.categoryId()).isEqualTo(CATEGORY_ID);
            assertThat(command.amount()).isEqualByComparingTo("100.00");
            assertThat(command.type()).isEqualTo(TransactionType.EXPENSE);
            assertThat(command.date()).isEqualTo(executionDate.atStartOfDay());
            assertThat(command.description()).isEqualTo("Rent (Recurring)");

            assertThat(template.getLastExecutedDate()).isEqualTo(executionDate);
            verify(plannedTransactionRepository).save(template);
        }

        @Test
        @DisplayName("Should execute without category when category does not exist")
        void shouldExecuteWithoutCategoryWhenCategoryDoesNotExist() {
            PlannedTransaction template = createTemplate();
            LocalDate executionDate = LocalDate.now();

            when(categoryExistencePort.existsForUser(CATEGORY_ID, USER_ID))
                    .thenReturn(false);
            when(registerTransactionUseCase.register(any()))
                    .thenAnswer(invocation -> {
                        var cmd = (RegisterTransactionUseCase.RegisterTransactionCommand)
                                invocation.getArgument(0);
                        return Transaction.create(
                                cmd.userId(), cmd.categoryId(), cmd.amount(),
                                cmd.date(), cmd.type(), cmd.description()
                        );
                    });

            executionService.execute(template, executionDate);

            ArgumentCaptor<RegisterTransactionUseCase.RegisterTransactionCommand> captor =
                    ArgumentCaptor.forClass(RegisterTransactionUseCase.RegisterTransactionCommand.class);
            verify(registerTransactionUseCase).register(captor.capture());

            assertThat(captor.getValue().categoryId()).isNull();
        }

        @Test
        @DisplayName("Should execute without category lookup when template has no category")
        void shouldSkipCategoryLookupWhenTemplateHasNoCategory() {
            PlannedTransaction template = PlannedTransaction.create(
                    USER_ID, null, new BigDecimal("50.00"),
                    TransactionType.INCOME, "Salary",
                    FrequencyType.MONTHS, 1,
                    LocalDate.now().minusMonths(1), null
            );
            LocalDate executionDate = LocalDate.now();

            when(registerTransactionUseCase.register(any()))
                    .thenAnswer(invocation -> {
                        var cmd = (RegisterTransactionUseCase.RegisterTransactionCommand)
                                invocation.getArgument(0);
                        return Transaction.create(
                                cmd.userId(), cmd.categoryId(), cmd.amount(),
                                cmd.date(), cmd.type(), cmd.description()
                        );
                    });

            executionService.execute(template, executionDate);

            ArgumentCaptor<RegisterTransactionUseCase.RegisterTransactionCommand> captor =
                    ArgumentCaptor.forClass(RegisterTransactionUseCase.RegisterTransactionCommand.class);
            verify(registerTransactionUseCase).register(captor.capture());

            assertThat(captor.getValue().categoryId()).isNull();
        }

        @Test
        @DisplayName("Should truncate long description and append recurring suffix")
        void shouldTruncateLongDescription() {
            String longDescription = "A".repeat(50);
            PlannedTransaction template = PlannedTransaction.create(
                    USER_ID, null, new BigDecimal("100.00"),
                    TransactionType.EXPENSE, longDescription,
                    FrequencyType.MONTHS, 1,
                    LocalDate.now().minusMonths(1), null
            );
            LocalDate executionDate = LocalDate.now();

            when(registerTransactionUseCase.register(any()))
                    .thenAnswer(invocation -> {
                        var cmd = (RegisterTransactionUseCase.RegisterTransactionCommand)
                                invocation.getArgument(0);
                        return Transaction.create(
                                cmd.userId(), cmd.categoryId(), cmd.amount(),
                                cmd.date(), cmd.type(), cmd.description()
                        );
                    });

            executionService.execute(template, executionDate);

            ArgumentCaptor<RegisterTransactionUseCase.RegisterTransactionCommand> captor =
                    ArgumentCaptor.forClass(RegisterTransactionUseCase.RegisterTransactionCommand.class);
            verify(registerTransactionUseCase).register(captor.capture());

            String resultDescription = captor.getValue().description();
            assertThat(resultDescription).endsWith(" (Recurring)");
            assertThat(resultDescription).hasSizeLessThanOrEqualTo(50);
        }

        @Test
        @DisplayName("Should preserve full description when short enough for suffix")
        void shouldPreserveShortDescriptionWithSuffix() {
            String shortDescription = "Rent";
            PlannedTransaction template = PlannedTransaction.create(
                    USER_ID, null, new BigDecimal("100.00"),
                    TransactionType.EXPENSE, shortDescription,
                    FrequencyType.MONTHS, 1,
                    LocalDate.now().minusMonths(1), null
            );
            LocalDate executionDate = LocalDate.now();

            when(registerTransactionUseCase.register(any()))
                    .thenAnswer(invocation -> {
                        var cmd = (RegisterTransactionUseCase.RegisterTransactionCommand)
                                invocation.getArgument(0);
                        return Transaction.create(
                                cmd.userId(), cmd.categoryId(), cmd.amount(),
                                cmd.date(), cmd.type(), cmd.description()
                        );
                    });

            executionService.execute(template, executionDate);

            ArgumentCaptor<RegisterTransactionUseCase.RegisterTransactionCommand> captor =
                    ArgumentCaptor.forClass(RegisterTransactionUseCase.RegisterTransactionCommand.class);
            verify(registerTransactionUseCase).register(captor.capture());

            assertThat(captor.getValue().description()).isEqualTo("Rent (Recurring)");
        }
    }
}