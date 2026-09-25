package com.puntomartinez.millete.plannedtransactions.infrastructure.in.controller;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction.FrequencyType;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.DeletePlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.ListPlannedTransactionsUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.RegisterPlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.RegisterPlannedTransactionUseCase.RegisterPlannedTransactionCommand;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.UpdatePlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.UpdatePlannedTransactionUseCase.UpdatePlannedTransactionCommand;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.CategoryDisplayPort;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.CategoryDisplayPort.CategoryDisplay;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlannedTransactionController")
class PlannedTransactionControllerTest {
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();

    @Mock private RegisterPlannedTransactionUseCase registerUseCase;
    @Mock private ListPlannedTransactionsUseCase listUseCase;
    @Mock private UpdatePlannedTransactionUseCase updateUseCase;
    @Mock private DeletePlannedTransactionUseCase deleteUseCase;
    @Mock private CategoryDisplayPort categoryDisplayPort;
    @Mock private Authentication authentication;

    @InjectMocks
    private PlannedTransactionController controller;

    private void mockAuthenticatedUser() {
        JwtUser jwtUser = new JwtUser(USER_ID, "user", "user@example.com");
        when(authentication.getPrincipal()).thenReturn(jwtUser);
    }

    private PlannedTransaction validTemplate() {
        return PlannedTransaction.reconstitute(
                UUID.randomUUID(), USER_ID, CATEGORY_ID,
                new BigDecimal("100.00"), TransactionType.EXPENSE,
                "Rent", FrequencyType.MONTHS, 1,
                LocalDate.now(), null,
                java.time.Instant.now(), java.time.Instant.now(),
                true, null, 0
        );
    }

    @Nested
    @DisplayName("registerPlannedTransaction")
    class Register {
        @Test
        @DisplayName("Should register and return 201 with category name")
        void shouldRegisterAndReturn201() {
            mockAuthenticatedUser();
            PlannedTransaction template = validTemplate();
            CategoryDisplay display = new CategoryDisplay(CATEGORY_ID, "Food", "#FF0000");
            RegisterPlannedTransactionRequestDTO request = new RegisterPlannedTransactionRequestDTO(
                    CATEGORY_ID, new BigDecimal("100.00"), TransactionType.EXPENSE,
                    "Rent", FrequencyType.MONTHS, 1, LocalDate.now(), null
            );
            when(registerUseCase.register(any())).thenReturn(template);
            when(categoryDisplayPort.findByIdAndUserId(CATEGORY_ID, USER_ID))
                    .thenReturn(Optional.of(display));

            ResponseEntity<PlannedTransactionResponseDTO> response =
                    controller.registerPlannedTransaction(request, authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().categoryName()).isEqualTo("Food");
            assertThat(response.getBody().categoryName()).isNotEqualTo("Sin categoría");

            ArgumentCaptor<RegisterPlannedTransactionCommand> captor =
                    ArgumentCaptor.forClass(RegisterPlannedTransactionCommand.class);
            verify(registerUseCase).register(captor.capture());
            assertThat(captor.getValue().userId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("Should use Sin categoría when category id is null")
        void shouldUseSinCategoriaWhenCategoryIdIsNull() {
            mockAuthenticatedUser();
            PlannedTransaction template = PlannedTransaction.reconstitute(
                    UUID.randomUUID(), USER_ID, null,
                    new BigDecimal("100.00"), TransactionType.EXPENSE,
                    "Rent", FrequencyType.MONTHS, 1,
                    LocalDate.now(), null,
                    java.time.Instant.now(), java.time.Instant.now(),
                    true, null, 0
            );
            RegisterPlannedTransactionRequestDTO request = new RegisterPlannedTransactionRequestDTO(
                    null, new BigDecimal("100.00"), TransactionType.EXPENSE,
                    "Rent", FrequencyType.MONTHS, 1, LocalDate.now(), null
            );
            when(registerUseCase.register(any())).thenReturn(template);

            ResponseEntity<PlannedTransactionResponseDTO> response =
                    controller.registerPlannedTransaction(request, authentication);

            assertThat(response.getBody().categoryName()).isEqualTo("Sin categoría");
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {
        @Test
        @DisplayName("Should reject negative page")
        void shouldRejectNegativePage() {
            assertThatThrownBy(() ->
                    controller.getAll(-1, 50, null, null, authentication)
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject zero or negative size")
        void shouldRejectNonPositiveSize() {
            assertThatThrownBy(() ->
                    controller.getAll(0, 0, null, null, authentication)
            ).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() ->
                    controller.getAll(0, -1, null, null, authentication)
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should return paginated response with category names")
        void shouldReturnPaginatedResponse() {
            mockAuthenticatedUser();
            PlannedTransaction template = validTemplate();
            CategoryDisplay display = new CategoryDisplay(CATEGORY_ID, "Food", "#FF0000");
            when(listUseCase.countByUserIdAndFilters(USER_ID, null, null))
                    .thenReturn(1L);
            when(listUseCase.findAllByUserId(
                    eq(USER_ID), anyInt(), anyInt(), any(), any()
            )).thenReturn(List.of(template));
            when(categoryDisplayPort.findByUserId(USER_ID))
                    .thenReturn(List.of(display));

            ResponseEntity<PaginatedResponseDTO<PlannedTransactionResponseDTO>> response =
                    controller.getAll(0, 50, null, null, authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().content()).hasSize(1);
            assertThat(response.getBody().content().get(0).categoryName())
                    .isEqualTo("Food");
        }

        @Test
        @DisplayName("Should use Sin categoría when category missing from map")
        void shouldUseSinCategoriaWhenCategoryMissing() {
            mockAuthenticatedUser();
            PlannedTransaction template = validTemplate();
            when(listUseCase.countByUserIdAndFilters(USER_ID, null, null))
                    .thenReturn(1L);
            when(listUseCase.findAllByUserId(
                    eq(USER_ID), anyInt(), anyInt(), any(), any()
            )).thenReturn(List.of(template));
            when(categoryDisplayPort.findByUserId(USER_ID))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<PaginatedResponseDTO<PlannedTransactionResponseDTO>> response =
                    controller.getAll(0, 50, null, null, authentication);

            assertThat(response.getBody().content().get(0).categoryName())
                    .isEqualTo("Sin categoría");
        }

        @Test
        @DisplayName("Should clamp page to last valid page when requested is out of range")
        void shouldClampPageToLastValid() {
            mockAuthenticatedUser();
            PlannedTransaction template = validTemplate();
            when(listUseCase.countByUserIdAndFilters(USER_ID, null, null))
                    .thenReturn(10L);
            when(listUseCase.findAllByUserId(
                    eq(USER_ID), anyInt(), anyInt(), any(), any()
            )).thenReturn(List.of(template));
            when(categoryDisplayPort.findByUserId(USER_ID))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<PaginatedResponseDTO<PlannedTransactionResponseDTO>> response =
                    controller.getAll(100, 10, null, null, authentication);

            assertThat(response.getBody().currentPage()).isLessThan(100);
        }
    }

    @Nested
    @DisplayName("updatePlannedTransaction")
    class Update {
        @Test
        @DisplayName("Should update and return 200")
        void shouldUpdateAndReturn200() {
            mockAuthenticatedUser();
            UUID id = UUID.randomUUID();
            PlannedTransaction updated = validTemplate();
            UpdatePlannedTransactionRequestDTO request =
                    new UpdatePlannedTransactionRequestDTO(
                            "Updated", TransactionType.EXPENSE,
                            new BigDecimal("200.00"),
                            FrequencyType.WEEKS, 2, null
                    );
            when(updateUseCase.update(eq(id), eq(USER_ID), any()))
                    .thenReturn(updated);
            when(categoryDisplayPort.findByIdAndUserId(any(), eq(USER_ID)))
                    .thenReturn(Optional.empty());

            ResponseEntity<PlannedTransactionResponseDTO> response =
                    controller.updatePlannedTransaction(id, request, authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<UpdatePlannedTransactionCommand> captor =
                    ArgumentCaptor.forClass(UpdatePlannedTransactionCommand.class);
            verify(updateUseCase).update(eq(id), eq(USER_ID), captor.capture());
            assertThat(captor.getValue().amount()).isEqualByComparingTo("200.00");
        }
    }

    @Nested
    @DisplayName("deletePlannedTransaction")
    class Delete {
        @Test
        @DisplayName("Should delete and return 204")
        void shouldDeleteAndReturn204() {
            mockAuthenticatedUser();
            UUID id = UUID.randomUUID();

            ResponseEntity<Void> response =
                    controller.deletePlannedTransaction(id, authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(deleteUseCase).deleteByIdAndUserId(id, USER_ID);
        }
    }
}