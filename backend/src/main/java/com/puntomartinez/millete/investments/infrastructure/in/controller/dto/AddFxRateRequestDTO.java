package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public record AddFxRateRequestDTO(@NotBlank @Pattern(regexp="[A-Za-z]{3}") String baseCurrency,
                                 @NotBlank @Pattern(regexp="[A-Za-z]{3}") String quoteCurrency,
                                 @NotNull Instant timestamp,
                                 @NotNull @DecimalMin(value="0.0000000000000001") BigDecimal rate,
                                 @NotBlank @Size(max=100) String source) { }
