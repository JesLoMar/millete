package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;

public final class Activity {
    private final UUID id;
    private final UUID userId;
    private final ActivityType type;
    private Instant occurredAt;
    private final Instant createdAt;
    private Instant modifiedAt;
    private long orderingKey;
    private UUID assetId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String currency;
    private BigDecimal secondaryAmount;
    private String secondaryCurrency;
    private BigDecimal ratio;
    private String localCurrency;
    private BigDecimal fxRateToLocal;
    private String fxRateSource;
    private Instant fxRateTimestamp;
    private BigDecimal amountInLocal;
    private String comment;
    private UUID linkedTransactionId;

    private Activity(UUID id, UUID userId, ActivityType type, Instant occurredAt,
                     Instant createdAt, Instant modifiedAt, long orderingKey,
                     UUID assetId, BigDecimal quantity, BigDecimal unitPrice,
                     BigDecimal amount, String currency, BigDecimal secondaryAmount,
                     String secondaryCurrency, BigDecimal ratio, String localCurrency,
                     BigDecimal fxRateToLocal, String fxRateSource,
                     Instant fxRateTimestamp, BigDecimal amountInLocal,
                     String comment, UUID linkedTransactionId) {
        this.id = required(id, "id");
        this.userId = required(userId, "userId");
        this.type = required(type, "type");
        this.occurredAt = required(occurredAt, "occurredAt");
        this.createdAt = required(createdAt, "createdAt");
        this.modifiedAt = required(modifiedAt, "modifiedAt");
        if (orderingKey < 0) throw new IllegalArgumentException("orderingKey cannot be negative");
        this.orderingKey = orderingKey;
        this.assetId = assetId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = amount;
        this.currency = currency == null ? null : currency(currency);
        this.secondaryAmount = secondaryAmount;
        this.secondaryCurrency = secondaryCurrency == null ? null : currency(secondaryCurrency);
        this.ratio = ratio;
        this.localCurrency = localCurrency == null ? null : currency(localCurrency);
        this.fxRateToLocal = fxRateToLocal;
        this.fxRateSource = fxRateSource;
        this.fxRateTimestamp = fxRateTimestamp;
        this.amountInLocal = amountInLocal;
        this.comment = comment == null ? null : comment.trim();
        this.linkedTransactionId = linkedTransactionId;
        validateShape();
    }

    public static Activity create(UUID userId, ActivityType type, Instant occurredAt,
                                  UUID assetId, BigDecimal quantity, BigDecimal unitPrice,
                                  BigDecimal amount, String currency,
                                  BigDecimal secondaryAmount, String secondaryCurrency,
                                  BigDecimal ratio, String localCurrency,
                                  BigDecimal fxRateToLocal, String fxRateSource,
                                  Instant fxRateTimestamp, BigDecimal amountInLocal,
                                  String comment, Instant now) {
        return new Activity(UUID.randomUUID(), userId, type, occurredAt, now, now,
                0, assetId, quantity, unitPrice, amount, currency, secondaryAmount,
                secondaryCurrency, ratio, localCurrency, fxRateToLocal,
                fxRateSource, fxRateTimestamp,
                amountInLocal, comment, null);
    }

    public static Activity reconstitute(UUID id, UUID userId, ActivityType type,
                                        Instant occurredAt, Instant createdAt,
                                        Instant modifiedAt, long orderingKey,
                                        UUID assetId, BigDecimal quantity,
                                        BigDecimal unitPrice, BigDecimal amount,
                                        String currency, BigDecimal secondaryAmount,
                                        String secondaryCurrency, BigDecimal ratio,
                                        String localCurrency, BigDecimal fxRateToLocal,
                                        String fxRateSource, Instant fxRateTimestamp,
                                        BigDecimal amountInLocal, String comment,
                                        UUID linkedTransactionId) {
        return new Activity(id, userId, type, occurredAt, createdAt, modifiedAt,
                orderingKey, assetId, quantity, unitPrice, amount, currency,
                secondaryAmount, secondaryCurrency, ratio, localCurrency,
                fxRateToLocal, fxRateSource, fxRateTimestamp,
                amountInLocal, comment, linkedTransactionId);
    }

    public void editComment(String value, Instant now) {
        if (type == ActivityType.DEPOSIT || type == ActivityType.WITHDRAW) {
            this.comment = value == null ? null : value.trim();
            this.modifiedAt = required(now, "now");
            return;
        }
        throw new IllegalStateException("Comment-only edit applies to DEPOSIT and WITHDRAW");
    }

    public void editInvestmentDetails(Instant occurredAt, BigDecimal quantity,
                                      BigDecimal unitPrice, BigDecimal amount,
                                      BigDecimal ratio, String comment,
                                      long orderingKey, String localCurrency,
                                      BigDecimal fxRateToLocal, BigDecimal amountInLocal,
                                      String fxRateSource, Instant fxRateTimestamp,
                                      Instant now) {
        if (type != ActivityType.BUY && type != ActivityType.SELL && type != ActivityType.SPLIT) {
            throw new IllegalStateException("This activity type cannot be edited with investment details");
        }
        this.validateEditableShape(occurredAt, quantity, unitPrice, amount, ratio);
        this.modifiedAt = required(now, "now");
        this.comment = comment == null ? null : comment.trim();
        this.occurredAt = occurredAt;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = amount;
        this.ratio = ratio;
        this.orderingKey = orderingKey;
        this.localCurrency = localCurrency;
        this.fxRateToLocal = fxRateToLocal;
        this.fxRateSource = fxRateSource;
        this.fxRateTimestamp = fxRateTimestamp;
        this.amountInLocal = amountInLocal;
        validateShape();
    }

    public void attachTransaction(UUID transactionId) {
        if (type != ActivityType.DEPOSIT && type != ActivityType.WITHDRAW) {
            throw new IllegalStateException("Only DEPOSIT and WITHDRAW link to daily transactions");
        }
        if (linkedTransactionId != null || transactionId == null) {
            throw new IllegalStateException("A valid transaction can be linked exactly once");
        }
        linkedTransactionId = transactionId;
    }

    private void validateEditableShape(Instant at, BigDecimal units, BigDecimal price,
                                       BigDecimal cashAmount, BigDecimal splitRatio) {
        required(at, "occurredAt");
        if (type == ActivityType.SPLIT) positive(splitRatio, "ratio");
        else {
            positive(units, "quantity");
            positive(price, "unitPrice");
            positive(cashAmount, "amount");
        }
    }

    private void validateShape() {
        switch (type) {
            case BUY, SELL -> {
                required(assetId, "assetId"); positive(quantity, "quantity");
                positive(unitPrice, "unitPrice"); positive(amount, "amount");
                required(currency, "currency");
                requireLocalFx();
            }
            case DIVIDEND, INTEREST, DEPOSIT, WITHDRAW, OPENING_CASH -> {
                positive(amount, "amount"); required(currency, "currency");
                requireLocalFx();
            }
            case SPLIT -> { required(assetId, "assetId"); positive(ratio, "ratio"); }
            case EXCHANGE -> {
                positive(amount, "source amount"); required(currency, "source currency");
                positive(secondaryAmount, "target amount"); required(secondaryCurrency, "target currency");
                positive(ratio, "exchange rate");
                if (currency.equals(secondaryCurrency)) throw new IllegalArgumentException("Exchange currencies must differ");
            }
        }
        if (comment != null && comment.length() > 500) throw new IllegalArgumentException("comment exceeds 500 characters");
        if (linkedTransactionId != null && type != ActivityType.DEPOSIT && type != ActivityType.WITHDRAW) {
            throw new IllegalArgumentException("Only DEPOSIT and WITHDRAW may link a daily transaction");
        }
    }

    private void requireLocalFx() {
        required(localCurrency, "localCurrency");
        positive(fxRateToLocal, "fxRateToLocal");
        required(fxRateSource, "fxRateSource");
        required(fxRateTimestamp, "fxRateTimestamp");
        positive(amountInLocal, "amountInLocal");
    }

    private static String currency(String value) {
        if (value == null || !value.matches("[A-Za-z]{3}")) throw new IllegalArgumentException("Currency must be ISO 4217");
        String code = value.toUpperCase(Locale.ROOT);
        Currency.getInstance(code);
        return code;
    }

    private static void positive(BigDecimal value, String name) {
        if (value == null || value.signum() <= 0) throw new IllegalArgumentException(name + " must be positive");
    }

    private static <T> T required(T value, String name) {
        if (value == null) throw new IllegalArgumentException(name + " is required");
        return value;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public ActivityType getType() { return type; }
    public Instant getOccurredAt() { return occurredAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getModifiedAt() { return modifiedAt; }
    public long getOrderingKey() { return orderingKey; }
    public UUID getAssetId() { return assetId; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public BigDecimal getSecondaryAmount() { return secondaryAmount; }
    public String getSecondaryCurrency() { return secondaryCurrency; }
    public BigDecimal getRatio() { return ratio; }
    public String getLocalCurrency() { return localCurrency; }
    public BigDecimal getFxRateToLocal() { return fxRateToLocal; }
    public String getFxRateSource() { return fxRateSource; }
    public Instant getFxRateTimestamp() { return fxRateTimestamp; }
    public BigDecimal getAmountInLocal() { return amountInLocal; }
    public String getComment() { return comment; }
    public UUID getLinkedTransactionId() { return linkedTransactionId; }
}
