package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import com.puntomartinez.millete.groupgoals.domain.model.GoalRole;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateMemberRequestDTO {

    private GoalRole role;

    @DecimalMin(value = "0.00", message = "El salario no puede ser negativo.")
    private BigDecimal salary;

    @DecimalMin(value = "0.00", message = "El porcentaje no puede ser negativo.")
    private BigDecimal customPercentage;
}