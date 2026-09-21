package com.puntomartinez.millete.transactions.infrastructure.in.controller;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.in.GetCategoryUseCase;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import com.puntomartinez.millete.transactions.application.services.TransactionPeriodService;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import com.puntomartinez.millete.transactions.domain.ports.in.DeleteTransactionUseCase;
import com.puntomartinez.millete.transactions.domain.ports.in.GetTransactionMetricsUseCase;
import com.puntomartinez.millete.transactions.domain.ports.in.GetTransactionUseCase;
import com.puntomartinez.millete.transactions.domain.ports.in.ListTransactionsUseCase;
import com.puntomartinez.millete.transactions.domain.ports.in.RegisterTransactionUseCase;
import com.puntomartinez.millete.transactions.domain.ports.in.RegisterTransactionUseCase.RegisterTransactionResult;
import com.puntomartinez.millete.transactions.domain.ports.in.UpdateTransactionUseCase;
import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.RegisterTransactionRequestDTO;
import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.TransactionMetricsResponseDTO;
import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.TransactionResponseDTO;
import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.UpdateTransactionRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionController")
class TransactionControllerTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private RegisterTransactionUseCase registerTransactionUseCase;

    @Mock
    private DeleteTransactionUseCase deleteTransactionUseCase;

    @Mock
    private GetTransactionUseCase getTransactionUseCase;

    @Mock
    private UpdateTransactionUseCase updateTransactionUseCase;

    @Mock
    private ListTransactionsUseCase listTransactionsUseCase;

    @Mock
    private GetTransactionMetricsUseCase transactionMetricsUseCase;

    @Mock
    private GetCategoryUseCase getCategoryUseCase;

    @Mock
    private TransactionPeriodService transactionPeriodService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TransactionController controller;

    private void mockAuthenticatedUser() {
        JwtUser jwtUser = new JwtUser(USER_ID, "username", "user@example.com");
        when(authentication.getPrincipal()).thenReturn(jwtUser);
    }

    private Transaction validTransaction() {
        return Transaction.create(
                USER_ID,
                UUID.randomUUID(),
                new BigDecimal("50.00"),
                LocalDateTime.now(),
                TransactionType.EXPENSE,
                "Groceries"
        );
    }

    @Nested
    @DisplayName("getMetrics")
    class GetMetrics {

        @Test
        @DisplayName("Should delegate to use case and map response")
        void shouldDelegateToUseCase() {
            mockAuthenticatedUser();

            GetTransactionMetricsUseCase.MetricsResult result =
                    new GetTransactionMetricsUseCase.MetricsResult(
                            new BigDecimal("1000.00"),
                            new BigDecimal("500.00"),
                            new BigDecimal("500.00"),
                            10L,
                            10.0,
                            -5.0,
                            15.0,
                            20.0
                    );

            when(transactionMetricsUseCase.getMetrics(any()))
                    .thenReturn(result);

            ResponseEntity<TransactionMetricsResponseDTO> response =
                    controller.getMetrics("month", authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().income())
                    .isEqualByComparingTo("1000.00");
            assertThat(response.getBody().expenses())
                    .isEqualByComparingTo("500.00");
            assertThat(response.getBody().balance())
                    .isEqualByComparingTo("500.00");
            assertThat(response.getBody().count()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("listTransactions")
    class ListTransactions {

        @ParameterizedTest
        @ValueSource(ints = {-1, -100})
        @DisplayName("Should reject negative page")
        void shouldRejectNegativePage(int page) {
            mockAuthenticatedUser();

            assertThatThrownBy(() ->
                    controller.listTransactions(
                            page, 50, null, null, "month", authentication
                    )
            ).isInstanceOf(InvalidInputException.class);

            verifyNoInteractions(listTransactionsUseCase);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        @DisplayName("Should reject non-positive page size")
        void shouldRejectNonPositivePageSize(int size) {
            mockAuthenticatedUser();

            assertThatThrownBy(() ->
                    controller.listTransactions(
                            0, size, null, null, "month", authentication
                    )
            ).isInstanceOf(InvalidInputException.class);

            verifyNoInteractions(listTransactionsUseCase);
        }

        @Test
        @DisplayName("Should reject invalid type parameter")
        void shouldRejectInvalidTypeParameter() {
            mockAuthenticatedUser();

            when(transactionPeriodService.getDateRange("month"))
                    .thenReturn(new LocalDateTime[]{
                            LocalDateTime.now(), LocalDateTime.now()
                    });

            assertThatThrownBy(() ->
                    controller.listTransactions(
                            0, 50, null, "INVALID", "month", authentication
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should return paginated response with category info")
        void shouldReturnPaginatedResponseWithCategoryInfo() {
            mockAuthenticatedUser();

            UUID categoryId = UUID.randomUUID();
            Transaction transaction = Transaction.create(
                    USER_ID,
                    categoryId,
                    new BigDecimal("50.00"),
                    LocalDateTime.now(),
                    TransactionType.EXPENSE,
                    "Groceries"
            );

            LocalDateTime[] range = {
                    LocalDateTime.now().minusDays(30),
                    LocalDateTime.now()
            };
            Category category = Category.create(
                    USER_ID, "Food", "#FF0000", null
            );

            when(transactionPeriodService.getDateRange("month"))
                    .thenReturn(range);
            when(listTransactionsUseCase.countByUserIdAndFilters(
                    eq(USER_ID), any(), any(), any(), any()
            )).thenReturn(1L);
            when(listTransactionsUseCase.findAllByUserId(
                    eq(USER_ID), anyInt(), anyInt(),
                    any(), any(), any(), any()
            )).thenReturn(List.of(transaction));
            when(getCategoryUseCase.findByUserId(USER_ID))
                    .thenReturn(List.of(category));

            ResponseEntity<PaginatedResponseDTO<TransactionResponseDTO>> response =
                    controller.listTransactions(
                            0, 50, null, null, "month", authentication
                    );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            PaginatedResponseDTO<TransactionResponseDTO> body =
                    Objects.requireNonNull(response.getBody());
            assertThat(body.content()).hasSize(1);

            TransactionResponseDTO firstItem = body.content().getFirst();
            assertThat(firstItem.categoryName()).isEqualTo("Food");
            assertThat(firstItem.categoryColor()).isEqualTo("#FF0000");
        }

        @Test
        @DisplayName("Should use Sin categoria when category not found in map")
        void shouldUseSinCategoriaWhenCategoryNotFound() {
            mockAuthenticatedUser();

            UUID categoryId = UUID.randomUUID();
            Transaction transaction = Transaction.create(
                    USER_ID,
                    categoryId,
                    new BigDecimal("50.00"),
                    LocalDateTime.now(),
                    TransactionType.EXPENSE,
                    "Unknown"
            );

            LocalDateTime[] range = {
                    LocalDateTime.now().minusDays(30),
                    LocalDateTime.now()
            };

            when(transactionPeriodService.getDateRange("month"))
                    .thenReturn(range);
            when(listTransactionsUseCase.countByUserIdAndFilters(
                    eq(USER_ID), any(), any(), any(), any()
            )).thenReturn(1L);
            when(listTransactionsUseCase.findAllByUserId(
                    eq(USER_ID), anyInt(), anyInt(),
                    any(), any(), any(), any()
            )).thenReturn(List.of(transaction));
            when(getCategoryUseCase.findByUserId(USER_ID))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<PaginatedResponseDTO<TransactionResponseDTO>> response =
                    controller.listTransactions(
                            0, 50, null, null, "month", authentication
                    );

            PaginatedResponseDTO<TransactionResponseDTO> body =
                    Objects.requireNonNull(response.getBody());
            TransactionResponseDTO firstItem = body.content().getFirst();
            assertThat(firstItem.categoryName())
                    .isEqualTo("Sin categoría");
        }

        @Test
        @DisplayName("Should pass type as null when not provided")
        void shouldPassTypeAsNullWhenNotProvided() {
            mockAuthenticatedUser();

            LocalDateTime[] range = {
                    LocalDateTime.now().minusDays(30),
                    LocalDateTime.now()
            };

            when(transactionPeriodService.getDateRange("month"))
                    .thenReturn(range);
            when(listTransactionsUseCase.countByUserIdAndFilters(
                    eq(USER_ID), any(), any(), any(), any()
            )).thenReturn(0L);
            when(listTransactionsUseCase.findAllByUserId(
                    eq(USER_ID), anyInt(), anyInt(),
                    any(), any(), any(), any()
            )).thenReturn(Collections.emptyList());

            ResponseEntity<PaginatedResponseDTO<TransactionResponseDTO>> response =
                    controller.listTransactions(
                            0, 50, null, null, "month", authentication
                    );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            PaginatedResponseDTO<TransactionResponseDTO> body =
                    Objects.requireNonNull(response.getBody());
            assertThat(body.content()).isEmpty();
        }

        @Test
        @DisplayName("Should parse valid type parameter correctly")
        void shouldParseValidTypeParameter() {
            mockAuthenticatedUser();

            LocalDateTime[] range = {
                    LocalDateTime.now().minusDays(30),
                    LocalDateTime.now()
            };

            when(transactionPeriodService.getDateRange("month"))
                    .thenReturn(range);
            when(listTransactionsUseCase.countByUserIdAndFilters(
                    eq(USER_ID), any(), eq(TransactionType.EXPENSE),
                    any(), any()
            )).thenReturn(0L);
            when(listTransactionsUseCase.findAllByUserId(
                    eq(USER_ID), anyInt(), anyInt(),
                    any(), eq(TransactionType.EXPENSE), any(), any()
            )).thenReturn(Collections.emptyList());

            controller.listTransactions(
                    0, 50, null, "expense", "month", authentication
            );

            verify(listTransactionsUseCase).countByUserIdAndFilters(
                    eq(USER_ID), any(), eq(TransactionType.EXPENSE),
                    any(), any()
            );
        }
    }

    @Nested
    @DisplayName("registerTransaction")
    class RegisterTransaction {

        @Test
        @DisplayName("Should register and return 201 with category info")
        void shouldRegisterAndReturn201() {
            mockAuthenticatedUser();

            RegisterTransactionRequestDTO request =
                    new RegisterTransactionRequestDTO(
                            null,
                            new BigDecimal("100.00"),
                            LocalDateTime.now(),
                            TransactionType.INCOME,
                            "Salary"
                    );
            Transaction transaction = validTransaction();

            when(registerTransactionUseCase.register(any()))
                    .thenReturn(new RegisterTransactionResult(
                            transaction, false
                    ));
            when(getCategoryUseCase.findByIdAndUserId(any(), eq(USER_ID)))
                    .thenThrow(new ResourceNotFoundException("Not found"));

            ResponseEntity<TransactionResponseDTO> response =
                    controller.registerTransaction(request, authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            TransactionResponseDTO body =
                    Objects.requireNonNull(response.getBody());
            assertThat(body.alertLimitExceeded()).isFalse();
        }

        @Test
        @DisplayName("Should use Sin categoria when category is null")
        void shouldUseSinCategoriaWhenCategoryIsNull() {
            mockAuthenticatedUser();

            RegisterTransactionRequestDTO request =
                    new RegisterTransactionRequestDTO(
                            null,
                            new BigDecimal("100.00"),
                            LocalDateTime.now(),
                            TransactionType.INCOME,
                            "Salary"
                    );
            Transaction transaction = Transaction.create(
                    USER_ID,
                    null,
                    new BigDecimal("100.00"),
                    LocalDateTime.now(),
                    TransactionType.INCOME,
                    "Salary"
            );

            when(registerTransactionUseCase.register(any()))
                    .thenReturn(new RegisterTransactionResult(
                            transaction, false
                    ));

            ResponseEntity<TransactionResponseDTO> response =
                    controller.registerTransaction(request, authentication);

            TransactionResponseDTO body =
                    Objects.requireNonNull(response.getBody());
            assertThat(body.categoryName()).isEqualTo("Sin categoría");
            assertThat(body.categoryColor()).isNull();
        }

        @Test
        @DisplayName("Should use Sin categoria when category lookup throws ResourceNotFoundException")
        void shouldUseSinCategoriaWhenLookupThrows() {
            mockAuthenticatedUser();

            UUID categoryId = UUID.randomUUID();
            RegisterTransactionRequestDTO request =
                    new RegisterTransactionRequestDTO(
                            categoryId,
                            new BigDecimal("100.00"),
                            LocalDateTime.now(),
                            TransactionType.EXPENSE,
                            "Expense"
                    );
            Transaction transaction = Transaction.create(
                    USER_ID,
                    categoryId,
                    new BigDecimal("100.00"),
                    LocalDateTime.now(),
                    TransactionType.EXPENSE,
                    "Expense"
            );

            when(registerTransactionUseCase.register(any()))
                    .thenReturn(new RegisterTransactionResult(
                            transaction, false
                    ));
            when(getCategoryUseCase.findByIdAndUserId(categoryId, USER_ID))
                    .thenThrow(new ResourceNotFoundException("Not found"));

            ResponseEntity<TransactionResponseDTO> response =
                    controller.registerTransaction(request, authentication);

            TransactionResponseDTO body =
                    Objects.requireNonNull(response.getBody());
            assertThat(body.categoryName()).isEqualTo("Sin categoría");
        }

        @Test
        @DisplayName("Should pass alertLimitExceeded flag from use case result")
        void shouldPassAlertLimitExceededFlag() {
            mockAuthenticatedUser();

            RegisterTransactionRequestDTO request =
                    new RegisterTransactionRequestDTO(
                            null,
                            new BigDecimal("100.00"),
                            LocalDateTime.now(),
                            TransactionType.EXPENSE,
                            "Big expense"
                    );
            Transaction transaction = validTransaction();

            when(registerTransactionUseCase.register(any()))
                    .thenReturn(new RegisterTransactionResult(
                            transaction, true
                    ));
            when(getCategoryUseCase.findByIdAndUserId(any(), eq(USER_ID)))
                    .thenThrow(new ResourceNotFoundException("Not found"));

            ResponseEntity<TransactionResponseDTO> response =
                    controller.registerTransaction(request, authentication);

            TransactionResponseDTO body =
                    Objects.requireNonNull(response.getBody());
            assertThat(body.alertLimitExceeded()).isTrue();
        }
    }

    @Nested
    @DisplayName("deleteTransaction")
    class DeleteTransaction {

        @Test
        @DisplayName("Should delegate and return 204")
        void shouldDelegateAndReturn204() {
            mockAuthenticatedUser();

            UUID transactionId = UUID.randomUUID();

            ResponseEntity<Void> response =
                    controller.deleteTransaction(transactionId, authentication);

            verify(deleteTransactionUseCase)
                    .deleteByIdAndUserId(transactionId, USER_ID);
            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NO_CONTENT);
        }
    }

    @Nested
    @DisplayName("getTransactionById")
    class GetTransactionById {

        @Test
        @DisplayName("Should return transaction when found")
        void shouldReturnTransactionWhenFound() {
            mockAuthenticatedUser();

            Transaction transaction = validTransaction();

            when(getTransactionUseCase.getByIdAndUserId(
                    transaction.getId(), USER_ID
            )).thenReturn(transaction);
            when(getCategoryUseCase.findByUserId(USER_ID))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<TransactionResponseDTO> response =
                    controller.getTransactionById(
                            transaction.getId(), authentication
                    );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            TransactionResponseDTO body =
                    Objects.requireNonNull(response.getBody());
            assertThat(body.id()).isEqualTo(transaction.getId());
        }
    }

    @Nested
    @DisplayName("updateTransaction")
    class UpdateTransaction {

        @Test
        @DisplayName("Should update and return 200")
        void shouldUpdateAndReturn200() {
            mockAuthenticatedUser();

            UUID transactionId = UUID.randomUUID();
            UpdateTransactionRequestDTO request =
                    new UpdateTransactionRequestDTO(
                            new BigDecimal("200.00"),
                            LocalDateTime.now(),
                            TransactionType.EXPENSE,
                            "Updated",
                            null
                    );
            Transaction transaction = validTransaction();

            when(updateTransactionUseCase.update(
                    eq(transactionId), any()
            )).thenReturn(transaction);
            when(getCategoryUseCase.findByIdAndUserId(any(), eq(USER_ID)))
                    .thenThrow(new ResourceNotFoundException("Not found"));

            ResponseEntity<TransactionResponseDTO> response =
                    controller.updateTransaction(
                            transactionId, request, authentication
                    );

            ArgumentCaptor<UpdateTransactionUseCase.UpdateTransactionCommand> captor =
                    ArgumentCaptor.forClass(
                            UpdateTransactionUseCase.UpdateTransactionCommand.class
                    );
            verify(updateTransactionUseCase)
                    .update(eq(transactionId), captor.capture());

            assertThat(captor.getValue().userId()).isEqualTo(USER_ID);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}