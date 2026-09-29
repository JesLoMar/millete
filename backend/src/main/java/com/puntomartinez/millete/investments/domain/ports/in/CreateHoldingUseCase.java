package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.Holding;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface CreateHoldingUseCase {

    Holding create(
            UUID userId,
            CreateHoldingCommand command
    );

    record CreateHoldingCommand(
            AssetReference assetReference,
            Instant snapshotAt,
            BigDecimal quantity,
            BigDecimal acquisitionCost
    ) {
    }
}