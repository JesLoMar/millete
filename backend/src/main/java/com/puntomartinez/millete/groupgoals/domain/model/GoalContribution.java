package com.puntomartinez.millete.groupgoals.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class GoalContribution {

    private final UUID id;
    private final UUID goalId;
    private final UUID userId;
    private BigDecimal amount;
    private LocalDateTime date;
    private final LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;

    private GoalContribution(
            UUID id,
            UUID goalId,
            UUID userId,
            BigDecimal amount,
            LocalDateTime date,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            boolean active) {

        this.id = requireId(id);
        this.goalId = requireGoalId(goalId);
        this.userId = requireUserId(userId);
        this.amount = requireAmount(amount);
        this.date = requireDate(date, "La fecha de la contribución es obligatoria.");
        this.createdAt = requireDate(createdAt, "La fecha de creación es obligatoria.");
        this.modifiedAt = requireDate(modifiedAt, "La fecha de modificación es obligatoria.");
        this.active = active;
    }

    public static GoalContribution create(
            UUID goalId,
            UUID userId,
            BigDecimal amount) {

        LocalDateTime now = LocalDateTime.now();

        return new GoalContribution(
                UUID.randomUUID(),
                goalId,
                userId,
                amount,
                now,
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
            LocalDateTime date,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            boolean active) {

        return new GoalContribution(
                id,
                goalId,
                userId,
                amount,
                date,
                createdAt,
                modifiedAt,
                active
        );
    }

    public void updateAmount(BigDecimal amount) {
        this.amount = requireAmount(amount);
        this.modifiedAt = LocalDateTime.now();
    }

    public void updateDate(LocalDateTime date) {
        this.date = requireDate(
                date,
                "La fecha de la contribución es obligatoria."
        );
        this.modifiedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.modifiedAt = LocalDateTime.now();
    }

    private static UUID requireId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El id de la contribución es obligatorio."
            );
        }
        return id;
    }

    private static UUID requireGoalId(UUID goalId) {
        if (goalId == null) {
            throw new IllegalArgumentException(
                    "La meta es obligatoria."
            );
        }
        return goalId;
    }

    private static UUID requireUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }
        return userId;
    }

    private static BigDecimal requireAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El importe de la contribución debe ser mayor que cero."
            );
        }
        return amount;
    }

    private static LocalDateTime requireDate(
            LocalDateTime value,
            String message) {

        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public UUID getId() {
        return id;
    }

    public UUID getGoalId() {
        return goalId;
    }

    public UUID getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public boolean isActive() {
        return active;
    }
}