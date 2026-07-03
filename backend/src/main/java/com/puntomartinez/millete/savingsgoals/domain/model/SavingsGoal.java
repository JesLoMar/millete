package com.puntomartinez.millete.savingsgoals.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SavingsGoal {

    private UUID id;
    private UUID userId;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate deadline;
    private String priority;
    private String status;
    private String link;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;

    public SavingsGoal() {
        this.id = UUID.randomUUID();
        this.currentAmount = BigDecimal.ZERO;
        this.priority = "MEDIUM";
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
        this.active = true;
    }

    public SavingsGoal(UUID id, UUID userId, String name, BigDecimal targetAmount,
                       BigDecimal currentAmount, LocalDate deadline, String priority,
                       String status, String link, LocalDateTime createdAt,
                       LocalDateTime modifiedAt, boolean active) {
        validateName(name);
        validateTargetAmount(targetAmount);
        validateCurrentAmount(currentAmount);
        validateDeadline(deadline);
        validatePriority(priority);
        validateStatus(status);

        this.id = (id != null) ? id : UUID.randomUUID();
        this.userId = userId;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = (currentAmount != null) ? currentAmount : BigDecimal.ZERO;
        this.deadline = deadline;
        this.priority = (priority != null) ? priority : "MEDIUM";
        this.status = (status != null) ? status : "ACTIVE";
        this.link = link;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
        this.modifiedAt = (modifiedAt != null) ? modifiedAt : LocalDateTime.now();
        this.active = active;
    }

    public void updateDetails(String name, BigDecimal targetAmount, LocalDate deadline,
                              String priority, String status, String link) {
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
        if (status != null) {
            validateStatus(status);
            this.status = status;
        }
        if (link != null) {
            this.link = link;
        }
        this.modifiedAt = LocalDateTime.now();
        recalculateStatus();
    }

    public void addContribution(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La contribución debe ser mayor que cero.");
        }
        this.currentAmount = this.currentAmount.add(amount);
        this.modifiedAt = LocalDateTime.now();
        recalculateStatus();
    }

    public void deactivate() {
        this.active = false;
        this.modifiedAt = LocalDateTime.now();
        if (!"COMPLETED".equals(this.status) && !"CANCELLED".equals(this.status)) {
            this.status = "CANCELLED";
        }
    }

    private void recalculateStatus() {
        if ("ACTIVE".equals(this.status) || "PAUSED".equals(this.status)) {
            if (this.currentAmount.compareTo(this.targetAmount) >= 0) {
                this.status = "COMPLETED";
            }
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del objetivo de ahorro es obligatorio.");
        }
    }

    private void validateTargetAmount(BigDecimal targetAmount) {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto objetivo debe ser mayor que cero.");
        }
    }

    private void validateCurrentAmount(BigDecimal currentAmount) {
        if (currentAmount != null && currentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto actual no puede ser negativo.");
        }
    }

    private void validateDeadline(LocalDate deadline) {
        if (deadline != null && !deadline.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha límite debe ser posterior a hoy.");
        }
    }

    private void validatePriority(String priority) {
        if (priority != null && !priority.matches("^(LOW|MEDIUM|HIGH)$")) {
            throw new IllegalArgumentException("Prioridad inválida. Valores permitidos: LOW, MEDIUM, HIGH.");
        }
    }

    private void validateStatus(String status) {
        if (status != null && !status.matches("^(ACTIVE|PAUSED|COMPLETED|CANCELLED)$")) {
            throw new IllegalArgumentException("Estado inválido. Valores permitidos: ACTIVE, PAUSED, COMPLETED, CANCELLED.");
        }
    }


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }

    public BigDecimal getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(BigDecimal currentAmount) { this.currentAmount = currentAmount; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(LocalDateTime modifiedAt) { this.modifiedAt = modifiedAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
