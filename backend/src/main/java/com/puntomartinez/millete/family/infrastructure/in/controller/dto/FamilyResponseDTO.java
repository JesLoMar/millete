package com.puntomartinez.millete.family.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class FamilyResponseDTO {
    private UUID id;
    private String name;
    private BigDecimal monthlyTarget;
    private String distributionMode;
}