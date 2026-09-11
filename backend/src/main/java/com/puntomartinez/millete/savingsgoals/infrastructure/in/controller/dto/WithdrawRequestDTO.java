package com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class WithdrawRequestDTO {

@NotNull(message = "La cantidad es obligatoria.")
@DecimalMin(
        value = "0.01",
        message = "La cantidad a retirar debe ser mayor que cero."
)
private BigDecimal amount;

public BigDecimal getAmount() {
    return amount;
}

public void setAmount(BigDecimal amount) {
    this.amount = amount;
}
}