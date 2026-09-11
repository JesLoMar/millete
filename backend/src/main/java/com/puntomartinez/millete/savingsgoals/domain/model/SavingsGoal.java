package com.puntomartinez.millete.savingsgoals.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class SavingsGoal {

    private final UUID id;
    private final UUID userId;

    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate deadline;
    private String priority;
    private String link;
    private final LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;

    private SavingsGoal(
            UUID id,
            UUID userId,
            String name,
            BigDecimal targetAmount,
            BigDecimal currentAmount,
            LocalDate deadline,
            String priority,
            String link,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            boolean active
    ) {
        validateUserId(userId);
        validateName(name);
        validateTargetAmount(targetAmount);
        validateCurrentAmount(currentAmount);
        validateDeadline(deadline);
        validatePriority(priority);

        this.id = id;
        this.userId = userId;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.deadline = deadline;
        this.priority = priority;
        this.link = link;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.active = active;
    }

    public static SavingsGoal create(
            UUID userId,
            String name,
            BigDecimal targetAmount,
            LocalDate deadline,
            String priority,
            String link
    ) {
        LocalDateTime now = LocalDateTime.now();

        return new SavingsGoal(
                UUID.randomUUID(),
                userId,
                name,
                targetAmount,
                BigDecimal.ZERO,
                deadline,
                priority != null ? priority : "MEDIUM",
                link,
                now,
                now,
                true
        );
    }

    public static SavingsGoal reconstitute(
            UUID id,
            UUID userId,
            String name,
            BigDecimal targetAmount,
            BigDecimal currentAmount,
            LocalDate deadline,
            String priority,
            String link,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            boolean active
    ) {
        validateId(id);
        validateCreatedAt(createdAt);
        validateModifiedAt(modifiedAt);

        return new SavingsGoal(
                id,
                userId,
                name,
                targetAmount,
                currentAmount,
                deadline,
                priority,
                link,
                createdAt,
                modifiedAt,
                active
        );
    }

    public void updateDetails(
            String name,
            BigDecimal targetAmount,
            LocalDate deadline,
            String priority,
            String link
    ) {
        if (name != null) {
            validateName(name);
            this.name = name;
        }

        if (targetAmount != null) {
            validateTargetAmount(targetAmount);
            this.targetAmount = targetAmount;
        }

        if (deadline != null) {
            validateDeadline(deadline);
            this.deadline = deadline;
        }

        if (priority != null) {
            validatePriority(priority);
            this.priority = priority;
        }

        if (link != null) {
            this.link = link;
        }

        this.modifiedAt = LocalDateTime.now();
    }

    public void addContribution(BigDecimal amount) {
        validatePositiveAmount(
                amount,
                "La contribución debe ser mayor que cero."
        );

        this.currentAmount = this.currentAmount.add(amount);
        this.modifiedAt = LocalDateTime.now();
    }

    public void withdraw(BigDecimal amount) {
        validatePositiveAmount(
                amount,
                "La cantidad a retirar debe ser mayor que cero."
        );

        if (this.currentAmount.compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "No se puede retirar más dinero del disponible en el objetivo."
            );
        }

        this.currentAmount = this.currentAmount.subtract(amount);
        this.modifiedAt = LocalDateTime.now();
    }

    public void deactivate() {
        if (!this.active) {
            return;
        }

        this.active = false;
        this.modifiedAt = LocalDateTime.now();
    }

    private static void validatePositiveAmount(
            BigDecimal amount,
            String message
    ) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El id del objetivo de ahorro es obligatorio."
            );
        }
    }

    private static void validateUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "El usuario del objetivo de ahorro es obligatorio."
            );
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del objetivo de ahorro es obligatorio."
            );
        }
    }

    private static void validateTargetAmount(BigDecimal targetAmount) {
        if (targetAmount == null
                || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto objetivo debe ser mayor que cero."
            );
        }
    }

    private static void validateCurrentAmount(BigDecimal currentAmount) {
        if (currentAmount == null
                || currentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El monto actual no puede ser negativo."
            );
        }
    }

    private static void validateDeadline(LocalDate deadline) {
        if (deadline != null && !deadline.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha límite debe ser posterior a hoy."
            );
        }
    }

    private static void validatePriority(String priority) {
        if (priority == null
                || !priority.matches("^(LOW|MEDIUM|HIGH)$")) {
            throw new IllegalArgumentException(
                    "Prioridad inválida. Valores permitidos: LOW, MEDIUM, HIGH."
            );
        }
    }

    private static void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de creación es obligatoria."
            );
        }
    }

    private static void validateModifiedAt(LocalDateTime modifiedAt) {
        if (modifiedAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de modificación es obligatoria."
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public String getPriority() {
        return priority;
    }

    public String getLink() {
        return link;
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