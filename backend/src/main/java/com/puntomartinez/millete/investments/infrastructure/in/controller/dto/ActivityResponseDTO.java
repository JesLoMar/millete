package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import com.puntomartinez.millete.investments.domain.model.ActivityType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ActivityResponseDTO(UUID id, ActivityType type, UUID assetId,
                                  Instant occurredAt, long orderingKey,
                                  BigDecimal quantity, BigDecimal unitPrice,
                                  BigDecimal amount, String currency,
                                  BigDecimal secondaryAmount, String secondaryCurrency,
                                  BigDecimal ratio, String localCurrency,
                                  BigDecimal fxRateToLocal, String fxRateSource,
                                  Instant fxRateTimestamp, BigDecimal amountInLocal,
                                  String comment, UUID linkedTransactionId) { }
