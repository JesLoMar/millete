package com.puntomartinez.millete.dashboard.infrastructure.in.controller;

import com.puntomartinez.millete.dashboard.domain.ports.in.GetDashboardDataUseCase;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.*;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardController")
class DashboardControllerTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private GetDashboardDataUseCase getDashboardDataUseCase;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DashboardController controller;

    @BeforeEach
    void setUp() {
        JwtUser jwtUser = new JwtUser(USER_ID, "testuser", "test@example.com");
        org.mockito.Mockito.lenient()
                .when(authentication.getPrincipal())
                .thenReturn(jwtUser);
    }

    @Nested
    @DisplayName("getMetrics")
    class GetMetrics {

        @Test
        @DisplayName("Should delegate to use case with correct userId and period")
        void shouldDelegateToUseCase() {
            DashboardMetricsResponseDTO expected =
                    new DashboardMetricsResponseDTO(
                            BigDecimal.TEN, BigDecimal.TEN,
                            BigDecimal.ZERO, BigDecimal.TEN,
                            0.0, 0.0, 0.0, 0.0
                    );
            when(getDashboardDataUseCase.getMetrics(USER_ID, "month"))
                    .thenReturn(expected);

            ResponseEntity<DashboardMetricsResponseDTO> response =
                    controller.getMetrics("month", authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(expected);
            verify(getDashboardDataUseCase).getMetrics(USER_ID, "month");
        }
    }

    @Nested
    @DisplayName("getHistory")
    class GetHistory {

        @Test
        @DisplayName("Should delegate to use case")
        void shouldDelegateToUseCase() {
            DashboardHistoryResponseDTO expected =
                    new DashboardHistoryResponseDTO(
                            "month", List.of("Sem 1"), List.of(BigDecimal.TEN)
                    );
            when(getDashboardDataUseCase.getHistory(USER_ID, "month"))
                    .thenReturn(expected);

            ResponseEntity<DashboardHistoryResponseDTO> response =
                    controller.getHistory("month", authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(expected);
        }
    }

    @Nested
    @DisplayName("getCategories")
    class GetCategories {

        @Test
        @DisplayName("Should delegate to use case")
        void shouldDelegateToUseCase() {
            DashboardCategoriesResponseDTO expected =
                    new DashboardCategoriesResponseDTO(
                            BigDecimal.TEN, Collections.emptyList()
                    );
            when(getDashboardDataUseCase.getCategories(USER_ID, "month"))
                    .thenReturn(expected);

            ResponseEntity<DashboardCategoriesResponseDTO> response =
                    controller.getCategories("month", authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(expected);
        }
    }

    @Nested
    @DisplayName("getBudgets")
    class GetBudgets {

        @Test
        @DisplayName("Should delegate to use case")
        void shouldDelegateToUseCase() {
            DashboardBudgetsResponseDTO expected =
                    new DashboardBudgetsResponseDTO("month", Collections.emptyList());
            when(getDashboardDataUseCase.getBudgets(USER_ID, "month"))
                    .thenReturn(expected);

            ResponseEntity<DashboardBudgetsResponseDTO> response =
                    controller.getBudgets("month", authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(expected);
        }
    }

    @Nested
    @DisplayName("getRecentTransactions")
    class GetRecentTransactions {

        @Test
        @DisplayName("Should pass valid limit directly")
        void shouldPassValidLimitDirectly() {
            DashboardTransactionsResponseDTO expected =
                    new DashboardTransactionsResponseDTO(Collections.emptyList());
            when(getDashboardDataUseCase.getRecentTransactions(USER_ID, 10))
                    .thenReturn(expected);

            controller.getRecentTransactions(10, authentication);

            verify(getDashboardDataUseCase).getRecentTransactions(USER_ID, 10);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100})
        @DisplayName("Should clamp limit to 1 when below minimum")
        void shouldClampLimitToMinimum(int limit) {
            DashboardTransactionsResponseDTO expected =
                    new DashboardTransactionsResponseDTO(Collections.emptyList());
            when(getDashboardDataUseCase.getRecentTransactions(USER_ID, 1))
                    .thenReturn(expected);

            controller.getRecentTransactions(limit, authentication);

            verify(getDashboardDataUseCase).getRecentTransactions(USER_ID, 1);
        }

        @ParameterizedTest
        @ValueSource(ints = {51, 100, 1000})
        @DisplayName("Should clamp limit to 50 when above maximum")
        void shouldClampLimitToMaximum(int limit) {
            DashboardTransactionsResponseDTO expected =
                    new DashboardTransactionsResponseDTO(Collections.emptyList());
            when(getDashboardDataUseCase.getRecentTransactions(USER_ID, 50))
                    .thenReturn(expected);

            controller.getRecentTransactions(limit, authentication);

            verify(getDashboardDataUseCase).getRecentTransactions(USER_ID, 50);
        }
    }

    @Nested
    @DisplayName("getSavingsGoals")
    class GetSavingsGoals {

        @Test
        @DisplayName("Should delegate to use case")
        void shouldDelegateToUseCase() {
            DashboardGoalsResponseDTO expected =
                    new DashboardGoalsResponseDTO(Collections.emptyList());
            when(getDashboardDataUseCase.getSavingsGoals(USER_ID))
                    .thenReturn(expected);

            ResponseEntity<DashboardGoalsResponseDTO> response =
                    controller.getSavingsGoals(authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(expected);
            verify(getDashboardDataUseCase).getSavingsGoals(USER_ID);
        }
    }
}