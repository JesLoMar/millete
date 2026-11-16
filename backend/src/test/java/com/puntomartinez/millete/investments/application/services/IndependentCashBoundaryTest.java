package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.ports.in.RecordDepositUseCase.RecordDepositCommand;
import com.puntomartinez.millete.investments.domain.ports.in.RecordWithdrawalUseCase.RecordWithdrawalCommand;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityIdempotencyRepository;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityOrderingPort;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityRepository;
import com.puntomartinez.millete.investments.domain.ports.out.FxRateRepository;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentPortfolioLockPort;
import com.puntomartinez.millete.investments.domain.ports.out.SharedAssetRepository;
import com.puntomartinez.millete.investments.domain.ports.out.TransactionIntegrationPort;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserCurrencyPort;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IndependentCashBoundaryTest {

    @Test
    void bothDirectionsReachTransfersWithCorrectLocalAmounts() {
        for (String mode : List.of("inverse", "direct", "same")) {
            UUID userId = UUID.randomUUID();
            Instant at = Instant.parse("2026-01-01T00:00:00Z");
            CurrencyCode eur = CurrencyCode.of("EUR");
            CurrencyCode usd = CurrencyCode.of("USD");
            List<Money> transfers = new ArrayList<>();

            ActivityRepository activities = mock(ActivityRepository.class);
            ActivityOrderingPort ordering = mock(ActivityOrderingPort.class);
            FxRateRepository rates = mock(FxRateRepository.class);
            UserCurrencyPort currencies = mock(UserCurrencyPort.class);
            TransactionIntegrationPort transactions = mock(TransactionIntegrationPort.class);
            TimeProvider clock = mock(TimeProvider.class);

            when(currencies.currencyAt(userId, at)).thenReturn(Optional.of(eur));
            when(activities.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(ordering.nextOrderingKey(userId, at)).thenReturn(1L, 2L);
            when(clock.now()).thenReturn(at);
            when(transactions.createInvestmentTransfer(
                    any(), any(), any(), any(), any(), any()
            )).thenAnswer(invocation -> {
                transfers.add(invocation.getArgument(3));
                return UUID.randomUUID();
            });

            if (mode.equals("inverse")) {
                when(rates.findLatestAt(usd, eur, at)).thenReturn(Optional.empty());
                when(rates.findLatestAt(eur, usd, at)).thenReturn(Optional.of(
                        new FxRate(UUID.randomUUID(), eur, usd, at,
                                new BigDecimal("1.1"), "test", at)
                ));
            } else if (mode.equals("direct")) {
                when(rates.findLatestAt(usd, eur, at)).thenReturn(Optional.of(
                        new FxRate(UUID.randomUUID(), usd, eur, at,
                                new BigDecimal("0.9"), "test", at)
                ));
            }

            CashActivityService service = new CashActivityService(
                    activities,
                    mock(ActivityIdempotencyRepository.class),
                    mock(InvestmentPortfolioLockPort.class),
                    ordering,
                    rates,
                    mock(SharedAssetRepository.class),
                    mock(UserAssetRepository.class),
                    currencies,
                    transactions,
                    mock(PortfolioRebuildService.class),
                    clock
            );
            String currency = mode.equals("same") ? "EUR" : "USD";

            service.record(userId, new RecordDepositCommand(
                    at, new BigDecimal("100"), currency, null
            ), "deposit");
            service.record(userId, new RecordWithdrawalCommand(
                    at, new BigDecimal("100"), currency, null
            ), "withdrawal");

            BigDecimal expected = new BigDecimal(switch (mode) {
                case "inverse" -> "90.909090909100";
                case "direct" -> "90";
                default -> "100";
            });
            assertEquals(2, transfers.size(), mode);
            for (Money amount : transfers) {
                assertEquals(eur, amount.currency(), mode);
                assertEquals(0, expected.compareTo(amount.amount()), mode);
            }
        }
    }
}
