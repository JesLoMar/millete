package com.puntomartinez.millete.family.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreateFamilyRequestDTO {
    private String name;
    private BigDecimal monthlyTarget;
    private String distributionMode; // EQUITATIVE, PROPORTIONAL, CUSTOM
}