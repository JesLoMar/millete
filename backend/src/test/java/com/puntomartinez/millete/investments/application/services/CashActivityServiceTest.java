package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.ports.in.RecordDepositUseCase.RecordDepositCommand;
import com.puntomartinez.millete.investments.domain.ports.in.RecordWithdrawalUseCase.RecordWithdrawalCommand;
import com.puntomartinez.millete.investments.domain.ports.out.*;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashActivityServiceTest {
    private static final UUID USER_ID = UUID.randomUUID();
    private static final Instant AT = Instant.parse("2026-01-01T00:00:00Z");

    @Mock ActivityRepository activities;
    @Mock ActivityIdempotencyRepository idempotency;
    @Mock InvestmentPortfolioLockPort portfolioLock;
    @Mock ActivityOrderingPort ordering;
    @Mock FxRateRepository fxRates;
    @Mock SharedAssetRepository sharedAssets;
    @Mock UserAssetRepository userAssets;
    @Mock UserCurrencyPort userCurrencies;
    @Mock TransactionIntegrationPort transactions;
    @Mock PortfolioRebuildService portfolioRebuild;
    @Mock TimeProvider time;

    @Test
    void inverseRateConvertsDepositAndWithdrawalAndPreservesLinkedActivities() {
        CashActivityService service = service();
        when(userCurrencies.currencyAt(USER_ID, AT))
                .thenReturn(Optional.of(CurrencyCode.of("EUR")));
        when(fxRates.findLatestAt(CurrencyCode.of("USD"), CurrencyCode.of("EUR"), AT))
                .thenReturn(Optional.empty());
        when(fxRates.findLatestAt(CurrencyCode.of("EUR"), CurrencyCode.of("USD"), AT))
                .thenReturn(Optional.of(new FxRate(
                        UUID.randomUUID(), CurrencyCode.of("EUR"), CurrencyCode.of("USD"),
                        AT, new BigDecimal("1.1"), "test", AT
                )));
        when(ordering.nextOrderingKey(USER_ID, AT)).thenReturn(1L, 2L);
        when(time.now()).thenReturn(AT);
        when(transactions.createInvestmentTransfer(
                eq(USER_ID), any(), any(), any(), eq(AT), any()
        )).thenReturn(UUID.randomUUID(), UUID.randomUUID());

        Activity deposit = service.record(
                USER_ID, new RecordDepositCommand(AT, new BigDecimal("100"), "USD", null), "deposit-key");
        Activity withdrawal = service.record(
                USER_ID, new RecordWithdrawalCommand(AT, new BigDecimal("100"), "USD", null), "withdraw-key");

        ArgumentCaptor<Money> amounts = ArgumentCaptor.forClass(Money.class);
        verify(transactions, times(2)).createInvestmentTransfer(
                eq(USER_ID), any(), any(), amounts.capture(), eq(AT), any()
        );
        assertEquals(CurrencyCode.of("EUR"), amounts.getAllValues().get(0).currency());
        assertEquals(new BigDecimal("90.909090909100"), amounts.getAllValues().get(0).amount());
        assertEquals(new BigDecimal("90.909090909100"), amounts.getAllValues().get(1).amount());
        assertEquals("USD", ((com.puntomartinez.millete.investments.domain.model.CashActivityDetails)
                deposit.getDetails()).amount().currency().value());
        assertEquals("USD", ((com.puntomartinez.millete.investments.domain.model.CashActivityDetails)
                withdrawal.getDetails()).amount().currency().value());
        verify(activities, times(4)).save(any());
        assertTrue(deposit.isLinkedCapitalMovement());
        assertTrue(withdrawal.isLinkedCapitalMovement());
    }

    @Test
    void directAndSameCurrencyConversionsKeepTheirAmounts() {
        CashActivityService service = service();
        when(userCurrencies.currencyAt(USER_ID, AT))
                .thenReturn(Optional.of(CurrencyCode.of("EUR")));
        when(fxRates.findLatestAt(CurrencyCode.of("USD"), CurrencyCode.of("EUR"), AT))
                .thenReturn(Optional.of(new FxRate(
                        UUID.randomUUID(), CurrencyCode.of("USD"), CurrencyCode.of("EUR"),
                        AT, new BigDecimal("0.9"), "test", AT
                )));
        when(ordering.nextOrderingKey(USER_ID, AT)).thenReturn(1L, 2L);
        when(time.now()).thenReturn(AT);
        when(transactions.createInvestmentTransfer(
                eq(USER_ID), any(), any(), any(), eq(AT), any()
        )).thenReturn(UUID.randomUUID(), UUID.randomUUID());

        service.record(USER_ID,
                new RecordDepositCommand(AT, new BigDecimal("100"), "USD", null), "direct-key");
        service.record(USER_ID,
                new RecordDepositCommand(AT, new BigDecimal("25"), "EUR", null), "same-key");

        ArgumentCaptor<Money> amounts = ArgumentCaptor.forClass(Money.class);
        verify(transactions, times(2)).createInvestmentTransfer(
                eq(USER_ID), any(), any(), amounts.capture(), eq(AT), any()
        );
        assertEquals(new BigDecimal("90.000000000000"), amounts.getAllValues().get(0).amount());
        assertEquals(new BigDecimal("25"), amounts.getAllValues().get(1).amount());
        verify(fxRates).findLatestAt(CurrencyCode.of("USD"), CurrencyCode.of("EUR"), AT);
        verify(fxRates, never()).findLatestAt(CurrencyCode.of("EUR"), CurrencyCode.of("USD"), AT);
    }

    private CashActivityService service() {
        when(idempotency.findByUserIdAndKey(eq(USER_ID), any())).thenReturn(Optional.empty());
        when(activities.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        return new CashActivityService(
                activities, idempotency, portfolioLock, ordering, fxRates,
                sharedAssets, userAssets, userCurrencies, transactions,
                portfolioRebuild, time
        );
    }
}
