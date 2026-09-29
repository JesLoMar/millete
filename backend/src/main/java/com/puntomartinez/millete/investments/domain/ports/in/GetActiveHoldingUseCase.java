package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.Holding;

import java.util.UUID;

public interface GetActiveHoldingUseCase {

    Holding get(
            UUID userId,
            AssetReference assetReference
    );
}