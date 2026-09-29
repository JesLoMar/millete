package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public final class PerformanceAttribution {

    private final Instant from;
    private final Instant to;
    private final CurrencyCode currency;

    private final Money openingValue;
    private final Money endingValue;

    private final Money openingCapital;
    private final Money contributions;
    private final Money withdrawals;

    private final Money realizedGains;
    private final Money unrealizedPriceEffect;
    private final Money fxEffect;

    private final Money cashDividends;
    private final Money inKindDividends;

    private final Money reconciliationDifference;

    private final boolean estimated;
    private final CalculationStatus status;
    private final String unavailableReason;

    private PerformanceAttribution(
            Instant from,
            Instant to,
            CurrencyCode currency,
            Money openingValue,
            Money endingValue,
            Money openingCapital,
            Money contributions,
            Money withdrawals,
            Money realizedGains,
            Money unrealizedPriceEffect,
            Money fxEffect,
            Money cashDividends,
            Money inKindDividends,
            Money reconciliationDifference,
            boolean estimated,
            CalculationStatus status,
            String unavailableReason
    ) {
        validateInterval(from, to);
        validateCurrency(currency);
        validateStatus(status);
        validateCalculatedFields(
                status,
                currency,
                openingValue,
                endingValue,
                openingCapital,
                contributions,
                withdrawals,
                realizedGains,
                unrealizedPriceEffect,
                fxEffect,
                cashDividends,
                inKindDividends,
                reconciliationDifference
        );
        validateUnavailableReason(
                status,
                unavailableReason
        );

        if (status == CalculationStatus.NOT_CALCULABLE
                && estimated) {

            throw new IllegalArgumentException(
                    "Una Performance no calculable no puede estar estimated"
            );
        }

        this.from = from;
        this.to = to;
        this.currency = currency;
        this.openingValue = openingValue;
        this.endingValue = endingValue;
        this.openingCapital = openingCapital;
        this.contributions = contributions;
        this.withdrawals = withdrawals;
        this.realizedGains = realizedGains;
        this.unrealizedPriceEffect = unrealizedPriceEffect;
        this.fxEffect = fxEffect;
        this.cashDividends = cashDividends;
        this.inKindDividends = inKindDividends;
        this.reconciliationDifference = reconciliationDifference;
        this.estimated = estimated;
        this.status = status;
        this.unavailableReason =
                unavailableReason == null
                        ? null
                        : unavailableReason.trim();
    }

    public static PerformanceAttribution calculable(
            Instant from,
            Instant to,
            CurrencyCode currency,
            Money openingValue,
            Money endingValue,
            Money openingCapital,
            Money contributions,
            Money withdrawals,
            Money realizedGains,
            Money unrealizedPriceEffect,
            Money fxEffect,
            Money cashDividends,
            Money inKindDividends,
            Money reconciliationDifference,
            boolean estimated
    ) {
        return new PerformanceAttribution(
                from,
                to,
                currency,
                openingValue,
                endingValue,
                openingCapital,
                contributions,
                withdrawals,
                realizedGains,
                unrealizedPriceEffect,
                fxEffect,
                cashDividends,
                inKindDividends,
                reconciliationDifference,
                estimated,
                CalculationStatus.CALCULABLE,
                null
        );
    }

    public static PerformanceAttribution notCalculable(
            Instant from,
            Instant to,
            CurrencyCode currency,
            Money openingValue,
            Money endingValue,
            String reason
    ) {
        return new PerformanceAttribution(
                from,
                to,
                currency,
                openingValue,
                endingValue,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                CalculationStatus.NOT_CALCULABLE,
                reason
        );
    }

    public BigDecimal portfolioChange() {
        if (openingValue == null
                || endingValue == null) {

            return null;
        }

        return endingValue.amount()
                .subtract(openingValue.amount());
    }

    public Money netExternalCapital() {
        requireCalculable();

        return openingCapital
                .add(contributions)
                .subtract(withdrawals);
    }

    private void requireCalculable() {
        if (status == CalculationStatus.NOT_CALCULABLE) {
            throw new IllegalStateException(
                    "La Performance no es calculable"
            );
        }
    }

    private static void validateInterval(
            Instant from,
            Instant to
    ) {
        if (from == null
                || to == null
                || !from.isBefore(to)) {

            throw new IllegalArgumentException(
                    "El intervalo debe tener from antes de to"
            );
        }
    }

    private static void validateCurrency(
            CurrencyCode currency
    ) {
        if (currency == null) {
            throw new IllegalArgumentException(
                    "La moneda de reporting es obligatoria"
            );
        }
    }

    private static void validateStatus(
            CalculationStatus status
    ) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "El estado del cálculo es obligatorio"
            );
        }
    }

    private static void validateCalculatedFields(
            CalculationStatus status,
            CurrencyCode currency,
            Money openingValue,
            Money endingValue,
            Money openingCapital,
            Money contributions,
            Money withdrawals,
            Money realizedGains,
            Money unrealizedPriceEffect,
            Money fxEffect,
            Money cashDividends,
            Money inKindDividends,
            Money reconciliationDifference
    ) {
        if (status == CalculationStatus.NOT_CALCULABLE) {
            return;
        }

        requireMoney(openingValue, "openingValue", currency);
        requireMoney(endingValue, "endingValue", currency);
        requireMoney(openingCapital, "openingCapital", currency);
        requireMoney(contributions, "contributions", currency);
        requireMoney(withdrawals, "withdrawals", currency);
        requireMoney(realizedGains, "realizedGains", currency);
        requireMoney(
                unrealizedPriceEffect,
                "unrealizedPriceEffect",
                currency
        );
        requireMoney(fxEffect, "fxEffect", currency);
        requireMoney(cashDividends, "cashDividends", currency);
        requireMoney(
                inKindDividends,
                "inKindDividends",
                currency
        );
        requireMoney(
                reconciliationDifference,
                "reconciliationDifference",
                currency
        );
    }

    private static void requireMoney(
            Money value,
            String name,
            CurrencyCode expectedCurrency
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    name + " es obligatorio"
            );
        }

        if (!expectedCurrency.equals(
                value.currency()
        )) {
            throw new IllegalArgumentException(
                    name + " debe utilizar la moneda de reporting"
            );
        }
    }

    private static void validateUnavailableReason(
            CalculationStatus status,
            String reason
    ) {
        if (status == CalculationStatus.NOT_CALCULABLE) {
            if (reason == null
                    || reason.isBlank()) {

                throw new IllegalArgumentException(
                        "Una Performance no calculable requiere unavailableReason"
                );
            }
        } else if (reason != null
                && !reason.isBlank()) {

            throw new IllegalArgumentException(
                    "Una Performance calculable no puede tener unavailableReason"
            );
        }
    }

    public Instant getFrom() {
        return from;
    }

    public Instant getTo() {
        return to;
    }

    public CurrencyCode getCurrency() {
        return currency;
    }

    public Money getOpeningValue() {
        return openingValue;
    }

    public Money getEndingValue() {
        return endingValue;
    }

    public Money getOpeningCapital() {
        return openingCapital;
    }

    public Money getContributions() {
        return contributions;
    }

    public Money getWithdrawals() {
        return withdrawals;
    }

    public Money getRealizedGains() {
        return realizedGains;
    }

    public Money getUnrealizedPriceEffect() {
        return unrealizedPriceEffect;
    }

    public Money getFxEffect() {
        return fxEffect;
    }

    public Money getCashDividends() {
        return cashDividends;
    }

    public Money getInKindDividends() {
        return inKindDividends;
    }

    public Money getReconciliationDifference() {
        return reconciliationDifference;
    }

    public boolean isEstimated() {
        return estimated;
    }

    public CalculationStatus getStatus() {
        return status;
    }

    public String getUnavailableReason() {
        return unavailableReason;
    }
}