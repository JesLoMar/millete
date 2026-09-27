package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/** Reconciled performance bridge for the interval (from, to], in one reporting currency. */
public record PerformanceAttribution(
        Instant from,
        Instant to,
        String currency,
        BigDecimal openingValue,
        BigDecimal endingValue,
        BigDecimal portfolioChange,
        BigDecimal contributions,
        BigDecimal withdrawals,
        BigDecimal netExternalFlows,
        BigDecimal realizedGains,
        BigDecimal unrealizedGainChange,
        BigDecimal dividends,
        BigDecimal interest,
        BigDecimal fxEffect,
        BigDecimal openingCashAdjustments,
        BigDecimal exchangeAdjustments,
        BigDecimal reconciliationDifference,
        boolean estimated,
        Status status,
        String unavailableReason) {

    public enum Status { CALCULABLE, ESTIMATED, NOT_CALCULABLE }
    private static final int MONEY_SCALE = 8;

    public PerformanceAttribution {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new IllegalArgumentException("Performance interval must have from before to");
        }
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("Reporting currency is required");
        currency = currency.trim().toUpperCase();
        if (status == null) throw new IllegalArgumentException("Performance status is required");
        if (status == Status.NOT_CALCULABLE && (fxEffect != null || reconciliationDifference != null)) {
            throw new IllegalArgumentException("Uncalculable performance cannot contain a reconciled FX effect");
        }
    }

    public static PerformanceAttribution reconcile(
            Instant from, Instant to, String currency,
            BigDecimal openingValue, BigDecimal endingValue,
            BigDecimal contributions, BigDecimal withdrawals,
            BigDecimal realizedGains, BigDecimal unrealizedGainChange,
            BigDecimal dividends, BigDecimal interest,
            BigDecimal openingCashAdjustments, BigDecimal exchangeAdjustments,
            boolean estimated) {
        BigDecimal change = endingValue.subtract(openingValue).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal netFlows = contributions.subtract(withdrawals).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal knownComponents = netFlows.add(realizedGains).add(unrealizedGainChange)
                .add(dividends).add(interest).add(openingCashAdjustments).add(exchangeAdjustments);
        BigDecimal fx = change.subtract(knownComponents).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal reconciliation = change.subtract(knownComponents.add(fx)).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        Status status = estimated ? Status.ESTIMATED : Status.CALCULABLE;
        return new PerformanceAttribution(from, to, currency, openingValue, endingValue, change,
                contributions, withdrawals, netFlows, realizedGains, unrealizedGainChange,
                dividends, interest, fx, openingCashAdjustments, exchangeAdjustments,
                reconciliation, estimated, status, null);
    }

    public static PerformanceAttribution notCalculable(
            Instant from, Instant to, String currency,
            BigDecimal openingValue, BigDecimal endingValue,
            String reason) {
        BigDecimal change = openingValue == null || endingValue == null
                ? null : endingValue.subtract(openingValue).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        return new PerformanceAttribution(from, to, currency, openingValue, endingValue, change,
                null, null, null, null, null, null, null, null, null, null, null,
                false, Status.NOT_CALCULABLE, reason);
    }
}
