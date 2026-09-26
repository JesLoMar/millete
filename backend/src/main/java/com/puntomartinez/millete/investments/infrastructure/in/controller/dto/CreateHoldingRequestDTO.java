package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateHoldingRequestDTO(@NotNull UUID assetId, @NotNull Instant snapshotAt,
                                      @NotNull @DecimalMin(value="0.000000000001") BigDecimal quantity,
                                      @NotNull @DecimalMin(value="0.0") BigDecimal acquisitionCost,
                                      @NotBlank @Pattern(regexp="[A-Za-z]{3}") String currency) { }
