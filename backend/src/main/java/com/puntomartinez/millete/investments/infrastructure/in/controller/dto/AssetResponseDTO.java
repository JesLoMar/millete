package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import com.puntomartinez.millete.investments.domain.model.AssetType;
import java.time.Instant;
import java.util.UUID;

public record AssetResponseDTO(UUID id, String name, String symbol, AssetType type,
                               UUID sectorId, String currency, boolean active,
                               Instant createdAt, Instant modifiedAt) { }
