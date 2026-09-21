package com.puntomartinez.millete.plannedtransactions.infrastructure.in.controller;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction.FrequencyType;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.DeletePlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.ListPlannedTransactionsUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.RegisterPlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.UpdatePlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.CategoryDisplayPort;
import com.puntomartinez.millete.plannedtransactions.infrastructure.in.controller.dto.PlannedTransactionResponseDTO;
import com.puntomartinez.millete.plannedtransactions.infrastructure.in.controller.dto.RegisterPlannedTransactionRequestDTO;
import com.puntomartinez.millete.plannedtransactions.infrastructure.in.controller.dto.UpdatePlannedTransactionRequestDTO;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
@DisplayName("PlannedTransactionController")
class PlannedTransactionControllerTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private RegisterPlannedTransactionUseCase registerUseCase;

    @Mock
    private ListPlannedTransactionsUseCase listPlannedTransactionsUseCase;

    @Mock
    private UpdatePlannedTransactionUseCase updateUseCase;

    @Mock
    private DeletePlannedTransactionUseCase deleteUseCase;

    @Mock
    private CategoryDisplayPort categoryDisplayPort;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PlannedTransactionController controller;

    private void mockAuthenticatedUser() {
        JwtUser jwtUser = new JwtUser(USER_ID, "username", "user@example.com");
        when(authentication.getPrincipal()).thenReturn(jwtUser);
    }

    private PlannedTransaction validPlannedTransaction() {
        return PlannedTransaction.create(
                USER_ID, UUID.randomUUID(), new BigDecimal("100.00"),
                TransactionType.EXPENSE, "Rent",
                FrequencyType.MONTHS, 1,
                LocalDate.now(), null
        );
    }

    @Nested
    @DisplayName("registerPlannedTransaction")
    class RegisterPlannedTransaction {

        @Test
        @DisplayName("Should register and return 201")
        void shouldRegisterAndReturn201() {
            mockAuthenticatedUser();

            RegisterPlannedTransactionRequestDTO request =
                    new RegisterPlannedTransactionRequestDTO(
                            UUID.randomUUID(),
                            new BigDecimal("100.00"),
                            TransactionType.EXPENSE,
                            "Rent",
                            FrequencyType.MONTHS,
                            1,
                            LocalDate.now(),
                            null
                    );
            PlannedTransaction ptx = validPlannedTransaction();

            when(registerUseCase.register(any())).thenReturn(ptx);
            when(categoryDisplayPort.findByUserId(USER_ID))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<PlannedTransactionResponseDTO> response =
                    controller.registerPlannedTransaction(request, authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            PlannedTransactionResponseDTO body =
                    Objects.requireNonNull(response.getBody());
            assertThat(body.id()).isEqualTo(ptx.getId());
            assertThat(body.categoryName()).isEqualTo("Sin categoría");
        }

        @Test
        @DisplayName("Should map category display when present")
        void shouldMapCategoryDisplayWhenPresent() {
            mockAuthenticatedUser();

            UUID categoryId = UUID.randomUUID();
            RegisterPlannedTransactionRequestDTO request =
                    new RegisterPlannedTransactionRequestDTO(
                            categoryId,
                            new BigDecimal("100.00"),
                            TransactionType.EXPENSE,
                            "Rent",
                            FrequencyType.MONTHS,
                            1,
                            LocalDate.now(),
                            null
                    );
            PlannedTransaction ptx = PlannedTransaction.create(
                    USER_ID, categoryId, new BigDecimal("100.00"),
                    TransactionType.EXPENSE, "Rent",
                    FrequencyType.MONTHS, 1,
                    LocalDate.now(), null
            );

            CategoryDisplayPort.CategoryDisplay display =
                    new CategoryDisplayPort.CategoryDisplay(categoryId, "Food", "#FF0000");

            when(registerUseCase.register(any())).thenReturn(ptx);
            when(categoryDisplayPort.findByIdAndUserId(categoryId, USER_ID))
                    .thenReturn(Optional.of(display));

            ResponseEntity<PlannedTransactionResponseDTO> response =
                    controller.registerPlannedTransaction(request, authentication);

            PlannedTransactionResponseDTO body =
                    Objects.requireNonNull(response.getBody());
            assertThat(body.categoryName()).isEqualTo("Food");
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @ParameterizedTest
        @ValueSource(ints = {-1, -100})
        @DisplayName("Should reject negative page")
        void shouldRejectNegativePage(int page) {
            mockAuthenticatedUser();

            assertThatThrownBy(() ->
                    controller.getAll(page, 50, null, null, authentication)
            ).isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(listPlannedTransactionsUseCase);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        @DisplayName("Should reject non-positive page size")
        void shouldRejectNonPositivePageSize(int size) {
            mockAuthenticatedUser();

            assertThatThrownBy(() ->
                    controller.getAll(0, size, null, null, authentication)
            ).isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(listPlannedTransactionsUseCase);
        }

        @Test
        @DisplayName("Should return paginated response with category info")
        void shouldReturnPaginatedResponseWithCategoryInfo() {
            mockAuthenticatedUser();

            UUID categoryId = UUID.randomUUID();
            PlannedTransaction ptx = PlannedTransaction.create(
                    USER_ID, categoryId, new BigDecimal("100.00"),
                    TransactionType.EXPENSE, "Rent",
                    FrequencyType.MONTHS, 1,
                    LocalDate.now(), null
            );

            CategoryDisplayPort.CategoryDisplay display =
                    new CategoryDisplayPort.CategoryDisplay(categoryId, "Food", "#FF0000");

            when(listPlannedTransactionsUseCase.countByUserIdAndFilters(
                    eq(USER_ID), any(), any()
            )).thenReturn(1L);
            when(listPlannedTransactionsUseCase.findAllByUserId(
                    eq(USER_ID), anyInt(), anyInt(), any(), any()
            )).thenReturn(List.of(ptx));
            when(categoryDisplayPort.findByUserId(USER_ID))
                    .thenReturn(List.of(display));

            ResponseEntity<PaginatedResponseDTO<PlannedTransactionResponseDTO>> response =
                    controller.getAll(0, 50, null, null, authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            PaginatedResponseDTO<PlannedTransactionResponseDTO> body =
                    Objects.requireNonNull(response.getBody());

            assertThat(body.content()).hasSize(1);
            PlannedTransactionResponseDTO firstItem = body.content().getFirst();
            assertThat(firstItem.categoryName()).isEqualTo("Food");
        }

        @Test
        @DisplayName("Should use Sin categoria when category not found in map")
        void shouldUseSinCategoriaWhenCategoryNotFound() {
            mockAuthenticatedUser();

            UUID categoryId = UUID.randomUUID();
            PlannedTransaction ptx = PlannedTransaction.create(
                    USER_ID, categoryId, new BigDecimal("100.00"),
                    TransactionType.EXPENSE, "Rent",
                    FrequencyType.MONTHS, 1,
                    LocalDate.now(), null
            );

            when(listPlannedTransactionsUseCase.countByUserIdAndFilters(
                    eq(USER_ID), any(), any()
            )).thenReturn(1L);
            when(listPlannedTransactionsUseCase.findAllByUserId(
                    eq(USER_ID), anyInt(), anyInt(), any(), any()
            )).thenReturn(List.of(ptx));
            when(categoryDisplayPort.findByUserId(USER_ID))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<PaginatedResponseDTO<PlannedTransactionResponseDTO>> response =
                    controller.getAll(0, 50, null, null, authentication);

            PaginatedResponseDTO<PlannedTransactionResponseDTO> body =
                    Objects.requireNonNull(response.getBody());
            PlannedTransactionResponseDTO firstItem = body.content().getFirst();
            assertThat(firstItem.categoryName()).isEqualTo("Sin categoría");
        }
    }

    @Nested
    @DisplayName("updatePlannedTransaction")
    class Update {

        @Test
        @DisplayName("Should update and return 200")
        void shouldUpdateAndReturn200() {
            mockAuthenticatedUser();

            UUID ptxId = UUID.randomUUID();
            UpdatePlannedTransactionRequestDTO request =
                    new UpdatePlannedTransactionRequestDTO(
                            "Updated",
                            TransactionType.EXPENSE,
                            new BigDecimal("200.00"),
                            FrequencyType.WEEKS,
                            2,
                            UUID.randomUUID()
                    );
            PlannedTransaction ptx = validPlannedTransaction();

            when(updateUseCase.update(eq(ptxId), eq(USER_ID), any()))
                    .thenReturn(ptx);
            when(categoryDisplayPort.findByUserId(USER_ID))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<PlannedTransactionResponseDTO> response =
                    controller.updatePlannedTransaction(ptxId, request, authentication);

            ArgumentCaptor<UpdatePlannedTransactionUseCase.UpdatePlannedTransactionCommand> captor =
                    ArgumentCaptor.forClass(UpdatePlannedTransactionUseCase.UpdatePlannedTransactionCommand.class);
            verify(updateUseCase).update(eq(ptxId), eq(USER_ID), captor.capture());

            assertThat(captor.getValue().amount()).isEqualByComparingTo("200.00");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("deletePlannedTransaction")
    class Delete {

        @Test
        @DisplayName("Should delegate and return 204")
        void shouldDelegateAndReturn204() {
            mockAuthenticatedUser();

            UUID ptxId = UUID.randomUUID();

            ResponseEntity<Void> response =
                    controller.deletePlannedTransaction(ptxId, authentication);

            verify(deleteUseCase).deleteByIdAndUserId(ptxId, USER_ID);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }
}