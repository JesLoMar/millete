package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import com.puntomartinez.millete.groupgoals.domain.model.DistributionMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateGoalRequestDTO {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres.")
    private String name;

    @NotNull(message = "El objetivo mensual es obligatorio.")
    @DecimalMin(value = "0.01", message = "El objetivo mensual debe ser mayor que cero.")
    private BigDecimal monthlyTarget;

    @NotNull(message = "El modo de distribución es obligatorio. Valores: EQUITATIVE, PROPORTIONAL, CUSTOM.")
    private DistributionMode distributionMode;
}