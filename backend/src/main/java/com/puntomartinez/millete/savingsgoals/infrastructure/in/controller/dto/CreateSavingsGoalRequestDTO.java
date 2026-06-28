package com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateSavingsGoalRequestDTO {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres.")
    private String name;

    @NotNull(message = "El monto objetivo es obligatorio.")
    @DecimalMin(value = "0.01", message = "El monto objetivo debe ser mayor que cero.")
    private BigDecimal targetAmount;

    private LocalDate deadline;

    @Pattern(regexp = "^(LOW|MEDIUM|HIGH)$", message = "Prioridad inválida. Valores: LOW, MEDIUM, HIGH.")
    private String priority;

    @Size(max = 500, message = "El link no puede exceder 500 caracteres.")
    private String link;

    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}