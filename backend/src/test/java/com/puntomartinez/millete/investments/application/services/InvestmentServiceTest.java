package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.model.Investment.InvestmentType;
import com.puntomartinez.millete.investments.domain.ports.in.RegisterInvestmentUseCase.RegisterInvestmentCommand;
import com.puntomartinez.millete.investments.domain.ports.in.UpdateInvestmentUseCase.UpdateInvestmentCommand;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvestmentService")
class InvestmentServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private InvestmentRepository investmentRepository;

    @InjectMocks
    private InvestmentService investmentService;

    private Investment validInvestment() {
        return Investment.create(
                USER_ID, "Apple Inc.", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"),
                InvestmentType.STOCK, LocalDateTime.now().minusDays(30)
        );
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("Should register investment successfully")
        void shouldRegisterInvestment() {
            RegisterInvestmentCommand command = new RegisterInvestmentCommand(
                    USER_ID, "Nvidia", "NVDA",
                    new BigDecimal("10"), new BigDecimal("100.00"),
                    InvestmentType.STOCK, LocalDateTime.now()
            );

            when(investmentRepository.save(any(Investment.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Investment result = investmentService.register(command);

            assertThat(result.getAssetName()).isEqualTo("Nvidia");
            assertThat(result.getTicker()).isEqualTo("NVDA");
            assertThat(result.getQuantity()).isEqualByComparingTo("10");
            assertThat(result.isActive()).isTrue();
            verify(investmentRepository).save(any(Investment.class));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("Should return investment when found")
        void shouldReturnInvestmentWhenFound() {
            Investment investment = validInvestment();

            when(investmentRepository.findByIdAndUserId(investment.getId(), USER_ID))
                    .thenReturn(Optional.of(investment));

            Investment result = investmentService.getById(investment.getId(), USER_ID);

            assertThat(result).isSameAs(investment);
        }

        @Test
        @DisplayName("Should throw when investment not found")
        void shouldThrowWhenInvestmentNotFound() {
            UUID investmentId = UUID.randomUUID();

            when(investmentRepository.findByIdAndUserId(investmentId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> investmentService.getById(investmentId, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findAllByUserId")
    class FindAllByUserId {

        @Test
        @DisplayName("Should delegate to repository")
        void shouldDelegateToRepository() {
            Investment inv1 = validInvestment();
            Investment inv2 = validInvestment();

            when(investmentRepository.findAllByUserId(USER_ID))
                    .thenReturn(List.of(inv1, inv2));

            List<Investment> result = investmentService.findAllByUserId(USER_ID);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delegate paginated query to repository")
        void shouldDelegatePaginatedQuery() {
            Investment inv1 = validInvestment();

            when(investmentRepository.findAllByUserId(
                    USER_ID, 0, 50, "apple", InvestmentType.STOCK
            )).thenReturn(List.of(inv1));

            List<Investment> result = investmentService.findAllByUserId(
                    USER_ID, 0, 50, "apple", InvestmentType.STOCK
            );

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should delegate count to repository")
        void shouldDelegateCount() {
            when(investmentRepository.countByUserIdAndFilters(
                    USER_ID, "apple", InvestmentType.STOCK
            )).thenReturn(5L);

            long result = investmentService.countByUserIdAndFilters(
                    USER_ID, "apple", InvestmentType.STOCK
            );

            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("updatePrice")
    class UpdatePrice {

        @Test
        @DisplayName("Should update price and save")
        void shouldUpdatePriceAndSave() {
            Investment investment = validInvestment();
            BigDecimal newPrice = new BigDecimal("200.00");

            when(investmentRepository.findByIdAndUserId(investment.getId(), USER_ID))
                    .thenReturn(Optional.of(investment));
            when(investmentRepository.save(any(Investment.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Investment result = investmentService.updatePrice(
                    investment.getId(), USER_ID, newPrice
            );

            assertThat(result.getCurrentPrice()).isEqualByComparingTo("200.00");
            verify(investmentRepository).save(investment);
        }

        @Test
        @DisplayName("Should throw when investment not found for price update")
        void shouldThrowWhenInvestmentNotFoundForPriceUpdate() {
            UUID investmentId = UUID.randomUUID();

            when(investmentRepository.findByIdAndUserId(investmentId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> investmentService.updatePrice(
                    investmentId, USER_ID, new BigDecimal("200.00")
            )).isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Should deactivate and save investment")
        void shouldDeactivateAndSaveInvestment() {
            Investment investment = validInvestment();

            when(investmentRepository.findByIdAndUserId(investment.getId(), USER_ID))
                    .thenReturn(Optional.of(investment));

            investmentService.delete(investment.getId(), USER_ID);

            ArgumentCaptor<Investment> captor = ArgumentCaptor.forClass(Investment.class);
            verify(investmentRepository).save(captor.capture());

            assertThat(captor.getValue().isActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw when investment not found for delete")
        void shouldThrowWhenInvestmentNotFoundForDelete() {
            UUID investmentId = UUID.randomUUID();

            when(investmentRepository.findByIdAndUserId(investmentId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> investmentService.delete(investmentId, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Should update investment details")
        void shouldUpdateInvestmentDetails() {
            Investment investment = validInvestment();
            UpdateInvestmentCommand command = new UpdateInvestmentCommand(
                    investment.getId(), USER_ID,
                    "Tesla Inc.", "TSLA",
                    new BigDecimal("5"), new BigDecimal("200.00"),
                    InvestmentType.STOCK, LocalDateTime.now()
            );

            when(investmentRepository.findByIdAndUserId(investment.getId(), USER_ID))
                    .thenReturn(Optional.of(investment));
            when(investmentRepository.save(any(Investment.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Investment result = investmentService.update(command);

            assertThat(result.getAssetName()).isEqualTo("Tesla Inc.");
            assertThat(result.getTicker()).isEqualTo("TSLA");
            verify(investmentRepository).save(investment);
        }

        @Test
        @DisplayName("Should throw when investment not found for update")
        void shouldThrowWhenInvestmentNotFoundForUpdate() {
            UUID investmentId = UUID.randomUUID();
            UpdateInvestmentCommand command = new UpdateInvestmentCommand(
                    investmentId, USER_ID,
                    "Tesla Inc.", "TSLA",
                    new BigDecimal("5"), new BigDecimal("200.00"),
                    InvestmentType.STOCK, LocalDateTime.now()
            );

            when(investmentRepository.findByIdAndUserId(investmentId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> investmentService.update(command))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}