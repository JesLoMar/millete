package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.Holding;

import java.util.List;
import java.util.UUID;

public interface ListHoldingsUseCase {

    List<Holding> list(
            UUID userId,
            AssetReference assetReference
    );
}