package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import com.puntomartinez.millete.investments.domain.model.ActivityType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RecordActivityRequestDTO(@NotNull ActivityType type,
                                       Instant occurredAt,
                                       UUID assetId,
                                       @DecimalMin(value="0.000000000001") BigDecimal quantity,
                                       @DecimalMin(value="0.000000000001") BigDecimal unitPrice,
                                       @DecimalMin(value="0.000000000001") BigDecimal amount,
                                       String currency,
                                       @DecimalMin(value="0.000000000001") BigDecimal secondaryAmount,
                                       String secondaryCurrency,
                                       @DecimalMin(value="0.0000000000000001") BigDecimal ratio,
                                       @DecimalMin(value="0.0000000000000001") BigDecimal exchangeRate,
                                       @Size(max=500) String comment) { }
