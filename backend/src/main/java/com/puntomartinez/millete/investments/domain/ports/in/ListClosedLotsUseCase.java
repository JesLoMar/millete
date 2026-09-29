package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.AssetReference;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ListClosedLotsUseCase {

    List<ClosedLotResult> list(
            UUID userId,
            Instant from,
            Instant to
    );

    record ClosedLotResult(
            UUID lotId,
            AssetReference assetReference,
            BigDecimal quantity,
            BigDecimal costBasis,
            String costBasisCurrency,
            BigDecimal proceeds,
            String proceedsCurrency,
            BigDecimal realizedGain,
            String realizedGainCurrency,
            Instant openedAt,
            Instant closedAt,
            boolean estimated,
            boolean historyIncomplete
    ) {
    }
}