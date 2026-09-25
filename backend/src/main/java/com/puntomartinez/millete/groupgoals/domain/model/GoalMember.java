package com.puntomartinez.millete.groupgoals.domain.model;

import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class GoalMember {

    private final UUID id;
    private final UUID goalId;
    private final UUID userId;
    private GoalRole role;
    private BigDecimal salary;
    private BigDecimal customPercentage;
    private Instant joinedAt;
    private final Instant createdAt;
    private Instant modifiedAt;
    private boolean active;

    private GoalMember(
            UUID id,
            UUID goalId,
            UUID userId,
            GoalRole role,
            BigDecimal salary,
            BigDecimal customPercentage,
            Instant joinedAt,
            Instant createdAt,
            Instant modifiedAt,
            boolean active) {

        this.id = requireId(id);
        this.goalId = requireGoalId(goalId);
        this.userId = requireUserId(userId);
        this.role = requireRole(role);
        this.salary = requireSalary(salary);
        this.customPercentage = requireCustomPercentage(customPercentage);
        this.joinedAt = requireDate(
                joinedAt,
                "La fecha de incorporación es obligatoria."
        );
        this.createdAt = requireDate(
                createdAt,
                "La fecha de creación es obligatoria."
        );
        this.modifiedAt = requireDate(
                modifiedAt,
                "La fecha de modificación es obligatoria."
        );
        this.active = active;
    }

    public static GoalMember create(
            TimeProvider timeProvider,
            UUID goalId,
            UUID userId,
            GoalRole role,
            BigDecimal salary) {

        Instant now = timeProvider.now();

        return new GoalMember(
                UUID.randomUUID(),
                goalId,
                userId,
                role,
                salary,
                null,
                now,
                now,
                now,
                true
        );
    }

    public static GoalMember create(
            TimeProvider timeProvider,
            UUID goalId,
            UUID userId,
            GoalRole role,
            BigDecimal salary,
            BigDecimal customPercentage) {

        Instant now = timeProvider.now();

        return new GoalMember(
                UUID.randomUUID(),
                goalId,
                userId,
                role,
                salary,
                customPercentage,
                now,
                now,
                now,
                true
        );
    }

    public static GoalMember reconstitute(
            UUID id,
            UUID goalId,
            UUID userId,
            GoalRole role,
            BigDecimal salary,
            BigDecimal customPercentage,
            Instant joinedAt,
            Instant createdAt,
            Instant modifiedAt,
            boolean active) {

        return new GoalMember(
                id,
                goalId,
                userId,
                role,
                salary,
                customPercentage,
                joinedAt,
                createdAt,
                modifiedAt,
                active
        );
    }

    public void updateDetails(
            TimeProvider timeProvider,
            GoalRole role,
            BigDecimal salary,
            BigDecimal customPercentage) {

        if (role != null) {
            this.role = requireRole(role);
        }

        if (salary != null) {
            this.salary = requireSalary(salary);
        }

        if (customPercentage != null) {
            this.customPercentage = requireCustomPercentage(customPercentage);
        }

        this.modifiedAt = timeProvider.now();
    }

    public void activate(TimeProvider timeProvider) {
        this.active = true;
        Instant now = timeProvider.now();
        this.joinedAt = now;
        this.modifiedAt = now;
    }

    public void deactivate(TimeProvider timeProvider) {
        this.active = false;
        this.modifiedAt = timeProvider.now();
    }

    public boolean isAdmin() {
        return GoalRole.ADMIN.equals(this.role);
    }

    private static UUID requireId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El id del miembro es obligatorio."
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

    private static GoalRole requireRole(GoalRole role) {
        if (role == null) {
            throw new IllegalArgumentException(
                    "El rol del miembro es obligatorio."
            );
        }
        return role;
    }

    private static BigDecimal requireSalary(BigDecimal salary) {
        if (salary == null) {
            return null;
        }

        if (salary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El salario no puede ser negativo."
            );
        }

        return salary;
    }

    private static BigDecimal requireCustomPercentage(
            BigDecimal customPercentage) {

        if (customPercentage == null) {
            return null;
        }

        if (customPercentage.compareTo(BigDecimal.ZERO) < 0
                || customPercentage.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException(
                    "El porcentaje personalizado debe estar entre 0 y 100."
            );
        }

        return customPercentage;
    }

    private static Instant requireDate(
            Instant value,
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

    public GoalRole getRole() {
        return role;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public BigDecimal getCustomPercentage() {
        return customPercentage;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public boolean isActive() {
        return active;
    }
}
