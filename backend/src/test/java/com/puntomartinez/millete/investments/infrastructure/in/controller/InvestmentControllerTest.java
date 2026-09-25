package com.puntomartinez.millete.investments.infrastructure.in.controller;

import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.model.Investment.InvestmentType;
import com.puntomartinez.millete.investments.domain.ports.in.*;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentDistributionResponseDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentEvolutionResponseDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentMetricsResponseDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentResponseDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.RegisterInvestmentRequestDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.UpdateInvestmentPriceRequestDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.UpdateInvestmentRequestDTO;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("InvestmentController")
class InvestmentControllerTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private RegisterInvestmentUseCase registerUseCase;

    @Mock
    private ListInvestmentsUseCase listUseCase;

    @Mock
    private GetInvestmentUseCase getUseCase;

    @Mock
    private UpdateInvestmentPriceUseCase updatePriceUseCase;

    @Mock
    private DeleteInvestmentUseCase deleteUseCase;

    @Mock
    private UpdateInvestmentUseCase updateUseCase;

    @Mock
    private GetInvestmentMetricsUseCase getInvestmentMetricsUseCase;

    @Mock
    private GetInvestmentEvolutionUseCase getInvestmentEvolutionUseCase;

    @Mock
    private GetInvestmentDistributionUseCase getInvestmentDistributionUseCase;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private InvestmentController controller;

    private void mockAuthenticatedUser() {
        JwtUser jwtUser = new JwtUser(USER_ID, "username", "user@example.com");
        when(authentication.getPrincipal()).thenReturn(jwtUser);
    }

    private Investment validInvestment() {
        return Investment.create(
                USER_ID, "Apple Inc.", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"),
                InvestmentType.STOCK, Instant.now().minusDays(30)
        );
    }

    @Nested
    @DisplayName("registerInvestment")
    class RegisterInvestment {

        @Test
        @DisplayName("Should register and return 201")
        void shouldRegisterAndReturn201() {
            mockAuthenticatedUser();

            RegisterInvestmentRequestDTO request = new RegisterInvestmentRequestDTO(
                    "Apple Inc.", "AAPL",
                    new BigDecimal("10"), new BigDecimal("150.00"),
                    InvestmentType.STOCK, Instant.now()
            );
            Investment investment = validInvestment();

            when(registerUseCase.register(any())).thenReturn(investment);

            ResponseEntity<InvestmentResponseDTO> response =
                    controller.registerInvestment(request, authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            InvestmentResponseDTO body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.assetName()).isEqualTo("Apple Inc.");
            assertThat(body.ticker()).isEqualTo("AAPL");
        }
    }

    @Nested
    @DisplayName("getAllInvestments")
    class GetAllInvestments {

        @Test
        @DisplayName("Should return paginated investments")
        void shouldReturnPaginatedInvestments() {
            mockAuthenticatedUser();

            Investment investment = validInvestment();

            when(listUseCase.countByUserIdAndFilters(eq(USER_ID), any(), any()))
                    .thenReturn(1L);
            when(listUseCase.findAllByUserId(
                    eq(USER_ID), anyInt(), anyInt(), any(), any()
            )).thenReturn(List.of(investment));

            ResponseEntity<PaginatedResponseDTO<InvestmentResponseDTO>> response =
                    controller.getAllInvestments(authentication, 0, 50, null, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            PaginatedResponseDTO<InvestmentResponseDTO> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.content()).hasSize(1);
            assertThat(body.content().getFirst().assetName()).isEqualTo("Apple Inc.");
        }
    }

    @Nested
    @DisplayName("getInvestment")
    class GetInvestment {

        @Test
        @DisplayName("Should return investment by id")
        void shouldReturnInvestmentById() {
            mockAuthenticatedUser();

            Investment investment = validInvestment();

            when(getUseCase.getById(investment.getId(), USER_ID))
                    .thenReturn(investment);

            ResponseEntity<InvestmentResponseDTO> response =
                    controller.getInvestment(investment.getId(), authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            InvestmentResponseDTO body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.id()).isEqualTo(investment.getId());
        }
    }

    @Nested
    @DisplayName("updateInvestment")
    class UpdateInvestment {

        @Test
        @DisplayName("Should update and return 200")
        void shouldUpdateAndReturn200() {
            mockAuthenticatedUser();

            Investment investment = validInvestment();
            UpdateInvestmentRequestDTO request = new UpdateInvestmentRequestDTO(
                    "Tesla Inc.", "TSLA",
                    new BigDecimal("5"), new BigDecimal("200.00"),
                    InvestmentType.STOCK, Instant.now()
            );

            when(updateUseCase.update(any())).thenReturn(investment);

            ResponseEntity<InvestmentResponseDTO> response =
                    controller.updateInvestment(investment.getId(), request, authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("updateInvestmentPrice")
    class UpdateInvestmentPrice {

        @Test
        @DisplayName("Should update price and return 200")
        void shouldUpdatePriceAndReturn200() {
            mockAuthenticatedUser();

            Investment investment = validInvestment();
            UpdateInvestmentPriceRequestDTO request =
                    new UpdateInvestmentPriceRequestDTO(new BigDecimal("200.00"));

            when(updatePriceUseCase.updatePrice(
                    eq(investment.getId()), eq(USER_ID), any()
            )).thenReturn(investment);

            ResponseEntity<InvestmentResponseDTO> response =
                    controller.updateInvestmentPrice(
                            investment.getId(), request, authentication
                    );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("deleteInvestment")
    class DeleteInvestment {

        @Test
        @DisplayName("Should delete and return 204")
        void shouldDeleteAndReturn204() {
            mockAuthenticatedUser();

            UUID investmentId = UUID.randomUUID();

            ResponseEntity<Void> response =
                    controller.deleteInvestment(investmentId, authentication);

            verify(deleteUseCase).delete(investmentId, USER_ID);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }

    @Nested
    @DisplayName("metrics endpoints")
    class MetricsEndpoints {

        @Test
        @DisplayName("Should return investment metrics")
        void shouldReturnInvestmentMetrics() {
            mockAuthenticatedUser();

            InvestmentMetricsResponseDTO metrics = new InvestmentMetricsResponseDTO(
                    new BigDecimal("1800.00"), new BigDecimal("300.00"),
                    BigDecimal.ZERO, 10.0, 20.0, 0.0
            );

            when(getInvestmentMetricsUseCase.getInvestmentMetrics(USER_ID, "month"))
                    .thenReturn(metrics);

            ResponseEntity<InvestmentMetricsResponseDTO> response =
                    controller.getInvestmentMetrics("month", authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Should return investment evolution")
        void shouldReturnInvestmentEvolution() {
            mockAuthenticatedUser();

            InvestmentEvolutionResponseDTO evolution = new InvestmentEvolutionResponseDTO(
                    "month", List.of("Jan", "Feb"), List.of(BigDecimal.TEN, BigDecimal.TEN)
            );

            when(getInvestmentEvolutionUseCase.getInvestmentEvolution(USER_ID, "month"))
                    .thenReturn(evolution);

            ResponseEntity<InvestmentEvolutionResponseDTO> response =
                    controller.getInvestmentEvolution("month", authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Should return investment distribution")
        void shouldReturnInvestmentDistribution() {
            mockAuthenticatedUser();

            InvestmentDistributionResponseDTO distribution =
                    new InvestmentDistributionResponseDTO(
                            new BigDecimal("1800.00"), Collections.emptyList()
                    );

            when(getInvestmentDistributionUseCase.getInvestmentDistribution(USER_ID, "month"))
                    .thenReturn(distribution);

            ResponseEntity<InvestmentDistributionResponseDTO> response =
                    controller.getInvestmentDistribution("month", authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }
    }
}