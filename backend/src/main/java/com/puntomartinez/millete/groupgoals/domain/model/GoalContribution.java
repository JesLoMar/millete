package com.puntomartinez.millete.groupgoals.domain.model;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class GoalContribution {

    private final UUID id;
    private final UUID goalId;
    private final UUID userId;
    private final BigDecimal amount;
    private final ContributionType type;
    private final LocalDateTime date;
    private final LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;

    private GoalContribution(
            UUID id,
            UUID goalId,
            UUID userId,
            BigDecimal amount,
            ContributionType type,
            LocalDateTime date,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            boolean active
    ) {
        validateId(id);
        validateGoalId(goalId);
        validateUserId(userId);
        validateAmount(amount);
        validateType(type);
        validateDate(date);
        validateCreatedAt(createdAt);
        validateModifiedAt(modifiedAt);

        this.id = id;
        this.goalId = goalId;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.active = active;
    }

    public static GoalContribution create(
            UUID goalId,
            UUID userId,
            BigDecimal amount,
            ContributionType type,
            LocalDateTime date
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new GoalContribution(
                UUID.randomUUID(),
                goalId,
                userId,
                amount,
                type,
                date,
                now,
                now,
                true
        );
    }

    public static GoalContribution reconstitute(
            UUID id,
            UUID goalId,
            UUID userId,
            BigDecimal amount,
            ContributionType type,
            LocalDateTime date,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            boolean active
    ) {
        return new GoalContribution(
                id, goalId, userId, amount, type,
                date, createdAt, modifiedAt, active
        );
    }

    public void deactivate() {
        if (!this.active) {
            return;
        }
        this.active = false;
        this.modifiedAt = LocalDateTime.now();
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new InvalidInputException(
                    "El id de la contribución es obligatorio"
            );
        }
    }

    private static void validateGoalId(UUID goalId) {
        if (goalId == null) {
            throw new InvalidInputException(
                    "El objetivo de la contribución es obligatorio"
            );
        }
    }

    private static void validateUserId(UUID userId) {
        if (userId == null) {
            throw new InvalidInputException(
                    "El usuario de la contribución es obligatorio"
            );
        }
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInputException(
                    "La cantidad de la contribución debe ser mayor que cero"
            );
        }
    }

    private static void validateType(ContributionType type) {
        if (type == null) {
            throw new InvalidInputException(
                    "El tipo de contribución es obligatorio"
            );
        }
    }

    private static void validateDate(LocalDateTime date) {
        if (date == null) {
            throw new InvalidInputException(
                    "La fecha de la contribución es obligatoria"
            );
        }
    }

    private static void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new InvalidInputException(
                    "La fecha de creación es obligatoria"
            );
        }
    }

    private static void validateModifiedAt(LocalDateTime modifiedAt) {
        if (modifiedAt == null) {
            throw new InvalidInputException(
                    "La fecha de modificación es obligatoria"
            );
        }
    }

    public UUID getId() { return id; }
    public UUID getGoalId() { return goalId; }
    public UUID getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public ContributionType getType() { return type; }
    public LocalDateTime getDate() { return date; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getModifiedAt() { return modifiedAt; }
    public boolean isActive() { return active; }
}