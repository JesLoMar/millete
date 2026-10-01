package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.ports.out.TransactionIntegrationPort;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import com.puntomartinez.millete.users.domain.model.UserPreferences;
import com.puntomartinez.millete.users.domain.ports.out.UserPreferencesRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Component
public final class TransactionIntegrationPostgresAdapter
        implements TransactionIntegrationPort {

    private final TransactionRepository transactions;
    private final UserPreferencesRepository preferences;
    private final TimeProvider time;

    public TransactionIntegrationPostgresAdapter(
            TransactionRepository transactions,
            UserPreferencesRepository preferences,
            TimeProvider time
    ) {
        this.transactions = transactions;
        this.preferences = preferences;
        this.time = time;
    }

    @Override
    public UUID createInvestmentTransfer(
            UUID userId,
            UUID activityId,
            TransferDirection direction,
            Money localAmount,
            Instant occurredAt,
            String description
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId es obligatorio."
            );
        }

        if (activityId == null) {
            throw new IllegalArgumentException(
                    "activityId es obligatorio."
            );
        }

        if (direction == null) {
            throw new IllegalArgumentException(
                    "La dirección de la transferencia es obligatoria."
            );
        }

        if (localAmount == null) {
            throw new IllegalArgumentException(
                    "localAmount es obligatorio."
            );
        }

        if (occurredAt == null) {
            throw new IllegalArgumentException(
                    "occurredAt es obligatorio."
            );
        }

        ZoneId zone =
                userTimeZone(userId);

        LocalDate date =
                occurredAt
                        .atZone(zone)
                        .toLocalDate();

        Transaction.TransactionType type =
                switch (direction) {
                    case TRANSFER_IN ->
                            Transaction.TransactionType.TRANSFER_IN;

                    case TRANSFER_OUT ->
                            Transaction.TransactionType.TRANSFER_OUT;
                };

        Transaction transaction =
                Transaction.createInvestmentTransfer(
                        time,
                        userId,
                        localAmount.amount(),
                        date,
                        type,
                        description,
                        localAmount.currency().value(),
                        activityId,
                        zone.getId()
                );

        Transaction saved =
                transactions.save(
                        transaction
                );

        return saved.getId();
    }

    @Override
    public boolean matchesInvestmentTransfer(
            UUID userId,
            UUID activityId,
            UUID transactionId,
            TransferDirection direction,
            Money localAmount
    ) {
        if (userId == null
                || activityId == null
                || transactionId == null
                || direction == null
                || localAmount == null) {
            return false;
        }

        Optional<Transaction> transaction =
                transactions.findByIdAndUserId(
                        transactionId,
                        userId
                );

        if (transaction.isEmpty()) {
            return false;
        }

        Transaction value =
                transaction.get();

        if (!value.isActive()) {
            return false;
        }

        if (!activityId.equals(
                value.getInvestmentActivityId()
        )) {
            return false;
        }

        Transaction.TransactionType expectedType =
                switch (direction) {
                    case TRANSFER_IN ->
                            Transaction.TransactionType.TRANSFER_IN;

                    case TRANSFER_OUT ->
                            Transaction.TransactionType.TRANSFER_OUT;
                };

        if (value.getType() != expectedType) {
            return false;
        }

        if (value.getCurrency() == null
                || !value.getCurrency().equals(
                        localAmount.currency().value()
                )) {
            return false;
        }

        return value.getAmount()
                .compareTo(
                        localAmount.amount()
                ) == 0;
    }

    private ZoneId userTimeZone(
            UUID userId
    ) {
        return preferences
                .findByUserId(userId)
                .map(UserPreferences::getPreferences)
                .map(preferences ->
                        preferences.get("timezone")
                )
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(this::parseZone)
                .orElse(
                        time.zone()
                );
    }

    private ZoneId parseZone(
            String value
    ) {
        try {
            return ZoneId.of(value);
        } catch (RuntimeException ignored) {
            return time.zone();
        }
    }
}