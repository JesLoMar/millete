package com.puntomartinez.millete.savingsgoals.domain.model;

import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import com.puntomartinez.millete.savingsgoals.domain.utils.GoalPriority;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

public class SavingsGoal {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_LINK_LENGTH = 500;

    private final UUID id;
    private final UUID userId;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate deadline;
    private GoalPriority priority;
    private String link;
    private final Instant createdAt;
    private Instant modifiedAt;
    private boolean active;

    private SavingsGoal(
            UUID id,
            UUID userId,
            String name,
            BigDecimal targetAmount,
            BigDecimal currentAmount,
            LocalDate deadline,
            GoalPriority priority,
            String link,
            Instant createdAt,
            Instant modifiedAt,
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
        this.link = normalizeAndValidateLink(link);
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.active = active;
    }

    public static SavingsGoal create(
            TimeProvider timeProvider,
            UUID userId,
            String name,
            BigDecimal targetAmount,
            LocalDate deadline,
            GoalPriority priority,
            String link
    ) {
        Instant now = timeProvider.instantNow();
        return new SavingsGoal(
                UUID.randomUUID(),
                userId,
                name,
                targetAmount,
                BigDecimal.ZERO,
                deadline,
                priority != null ? priority : GoalPriority.MEDIUM,
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
            GoalPriority priority,
            String link,
            Instant createdAt,
            Instant modifiedAt,
            boolean active
    ) {
        validateId(id);
        validateCreatedAt(createdAt);
        validateModifiedAt(modifiedAt);
        return new SavingsGoal(
                id, userId, name, targetAmount, currentAmount,
                deadline, priority, link, createdAt, modifiedAt, active
        );
    }

    public void updateDetails(TimeProvider timeProvider, 
            String name,
            BigDecimal targetAmount,
            LocalDate deadline,
            GoalPriority priority,
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
            if (link.isBlank()) {
                this.link = null;
            } else {
                this.link = normalizeAndValidateLink(link);
            }
        }
        this.modifiedAt = timeProvider.instantNow();
    }

    public void addContribution(TimeProvider timeProvider, BigDecimal amount) {
        validatePositiveAmount(
                amount,
                "La contribución debe ser mayor que cero."
        );
        this.currentAmount = this.currentAmount.add(amount);
        this.modifiedAt = timeProvider.instantNow();
    }

    public void withdraw(TimeProvider timeProvider, BigDecimal amount) {
        validatePositiveAmount(
                amount,
                "La cantidad a retirar debe ser mayor que cero."
        );
        if (this.currentAmount.compareTo(amount) < 0) {
            throw new InvalidInputException(
                    "No se puede retirar más dinero del disponible en el objetivo."
            );
        }
        this.currentAmount = this.currentAmount.subtract(amount);
        this.modifiedAt = timeProvider.instantNow();
    }

    public void deactivate(TimeProvider timeProvider) {
        if (!this.active) {
            return;
        }
        this.active = false;
        this.modifiedAt = timeProvider.instantNow();
    }

    private static void validatePositiveAmount(
            BigDecimal amount, String message
    ) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInputException(message);
        }
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new InvalidInputException(
                    "El id del objetivo de ahorro es obligatorio."
            );
        }
    }

    private static void validateUserId(UUID userId) {
        if (userId == null) {
            throw new InvalidInputException(
                    "El usuario del objetivo de ahorro es obligatorio."
            );
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException(
                    "El nombre del objetivo de ahorro es obligatorio."
            );
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidInputException(
                    "El nombre no puede superar los "
                            + MAX_NAME_LENGTH + " caracteres."
            );
        }
    }

    private static void validateTargetAmount(BigDecimal targetAmount) {
        if (targetAmount == null
                || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInputException(
                    "El monto objetivo debe ser mayor que cero."
            );
        }
    }

    private static void validateCurrentAmount(BigDecimal currentAmount) {
        if (currentAmount == null
                || currentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInputException(
                    "El monto actual no puede ser negativo."
            );
        }
    }

    private static void validateDeadline(LocalDate deadline) {
        if (deadline != null && !deadline.isAfter(LocalDate.now())) {
            throw new InvalidInputException(
                    "La fecha límite debe ser posterior a hoy."
            );
        }
    }

    private static void validatePriority(GoalPriority priority) {
        if (priority == null) {
            throw new InvalidInputException(
                    "La prioridad del objetivo es obligatoria."
            );
        }
    }

    private static void validateCreatedAt(Instant createdAt) {
        if (createdAt == null) {
            throw new InvalidInputException(
                    "La fecha de creación es obligatoria."
            );
        }
    }

    private static void validateModifiedAt(Instant modifiedAt) {
        if (modifiedAt == null) {
            throw new InvalidInputException(
                    "La fecha de modificación es obligatoria."
            );
        }
    }

    private static String normalizeAndValidateLink(String link) {
        if (link == null || link.isBlank()) {
            return null;
        }
        String candidate = link.trim();
        if (!candidate.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) {
            candidate = "https://" + candidate;
        }
        if (candidate.length() > MAX_LINK_LENGTH) {
            throw new InvalidInputException(
                    "El enlace no puede exceder "
                            + MAX_LINK_LENGTH + " caracteres."
            );
        }
        try {
            URI uri = new URI(candidate);
            String scheme = uri.getScheme();
            boolean validScheme = scheme != null
                    && (scheme.equalsIgnoreCase("http")
                    || scheme.equalsIgnoreCase("https"));
            if (!validScheme || uri.getHost() == null) {
                throw new InvalidInputException(
                        "El enlace debe ser una URL válida (http o https)."
                );
            }
            return candidate;
        } catch (URISyntaxException e) {
            throw new InvalidInputException(
                    "El enlace debe ser una URL válida (http o https)."
            );
        }
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public BigDecimal getCurrentAmount() { return currentAmount; }
    public LocalDate getDeadline() { return deadline; }
    public GoalPriority getPriority() { return priority; }
    public String getLink() { return link; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getModifiedAt() { return modifiedAt; }
    public boolean isActive() { return active; }
}