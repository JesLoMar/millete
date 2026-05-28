package com.puntomartinez.millete.family.infrastructure.in.controller.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AddContributionRequestDTO {
    private BigDecimal amount;
}