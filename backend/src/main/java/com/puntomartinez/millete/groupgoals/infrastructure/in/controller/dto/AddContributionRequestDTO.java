package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AddContributionRequestDTO {
    private BigDecimal amount;
}
