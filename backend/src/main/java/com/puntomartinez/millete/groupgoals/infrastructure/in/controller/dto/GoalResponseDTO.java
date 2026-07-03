package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class GoalResponseDTO {
    private UUID id;
    private String name;
    private BigDecimal monthlyTarget;
    private String distributionMode;
}
