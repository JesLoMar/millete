package com.puntomartinez.millete.groupgoals.domain.model;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;

import java.math.BigDecimal;
import java.time.Instant;
import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import java.util.UUID;

public class GoalUnit {

    private static final int MAX_NAME_LENGTH = 100;

    private final UUID id;
    private String name;
    private BigDecimal monthlyTarget;
    private DistributionMode distributionMode;
    private final Instant createdAt;
    private Instant modifiedAt;
    private boolean active;

    private GoalUnit(
            UUID id,
            String name,
            BigDecimal monthlyTarget,
            DistributionMode distributionMode,
            Instant createdAt,
            Instant modifiedAt,
            boolean active
    ) {
        validateId(id);
        validateName(name);
        validateMonthlyTarget(monthlyTarget);
        validateDistributionMode(distributionMode);
        validateCreatedAt(createdAt);
        validateModifiedAt(modifiedAt);

        this.id = id;
        this.name = name;
        this.monthlyTarget = monthlyTarget;
        this.distributionMode = distributionMode;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.active = active;
    }

    public static GoalUnit create(
            TimeProvider timeProvider,
            String name,
            BigDecimal monthlyTarget,
            DistributionMode distributionMode
    ) {
        Instant now = timeProvider.instantNow();
        return new GoalUnit(
                UUID.randomUUID(),
                name,
                monthlyTarget,
                distributionMode,
                now,
                now,
                true
        );
    }

    public static GoalUnit reconstitute(
            UUID id,
            String name,
            BigDecimal monthlyTarget,
            DistributionMode distributionMode,
            Instant createdAt,
            Instant modifiedAt,
            boolean active
    ) {
        return new GoalUnit(
                id, name, monthlyTarget, distributionMode,
                createdAt, modifiedAt, active
        );
    }

    public void updateDetails(
            TimeProvider timeProvider,
            String name,
            BigDecimal monthlyTarget,
            DistributionMode distributionMode
    ) {
        if (name != null) {
            validateName(name);
            this.name = name;
        }
        if (monthlyTarget != null) {
            validateMonthlyTarget(monthlyTarget);
            this.monthlyTarget = monthlyTarget;
        }
        if (distributionMode != null) {
            validateDistributionMode(distributionMode);
            this.distributionMode = distributionMode;
        }
        this.modifiedAt = timeProvider.instantNow();
    }

    public void deactivate(TimeProvider timeProvider) {
        if (!this.active) {
            return;
        }
        this.active = false;
        this.modifiedAt = timeProvider.instantNow();
    }

    // ── Validaciones ──────────────────────────────────────────

    private static void validateId(UUID id) {
        if (id == null) {
            throw new InvalidInputException(
                    "El id de la meta es obligatorio"
            );
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException(
                    "El nombre de la meta es obligatorio"
            );
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidInputException(
                    "El nombre no puede exceder los "
                            + MAX_NAME_LENGTH + " caracteres"
            );
        }
    }

    private static void validateMonthlyTarget(BigDecimal monthlyTarget) {
        if (monthlyTarget == null) {
            throw new InvalidInputException(
                    "El objetivo mensual es obligatorio"
            );
        }
        if (monthlyTarget.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInputException(
                    "El objetivo mensual no puede ser negativo"
            );
        }
    }

    private static void validateDistributionMode(
            DistributionMode distributionMode
    ) {
        if (distributionMode == null) {
            throw new InvalidInputException(
                    "El modo de distribución es obligatorio"
            );
        }
    }

    private static void validateCreatedAt(Instant createdAt) {
        if (createdAt == null) {
            throw new InvalidInputException(
                    "La fecha de creación es obligatoria"
            );
        }
    }

    private static void validateModifiedAt(Instant modifiedAt) {
        if (modifiedAt == null) {
            throw new InvalidInputException(
                    "La fecha de modificación es obligatoria"
            );
        }
    }

    // ── Getters ───────────────────────────────────────────────

    public UUID getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getMonthlyTarget() { return monthlyTarget; }
    public DistributionMode getDistributionMode() { return distributionMode; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getModifiedAt() { return modifiedAt; }
    public boolean isActive() { return active; }
}