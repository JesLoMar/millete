package com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AddContributionRequestDTO {

    @NotNull(message = "La cantidad es obligatoria.")
    @DecimalMin(value = "0.01", message = "La contribución debe ser mayor que cero.")
    private BigDecimal amount;

    // Getters y Setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}