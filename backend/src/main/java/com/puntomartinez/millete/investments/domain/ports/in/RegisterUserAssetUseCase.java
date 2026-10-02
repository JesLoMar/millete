package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.AssetOrigin;
import com.puntomartinez.millete.investments.domain.model.AssetType;
import com.puntomartinez.millete.investments.domain.model.UserAsset;

import java.math.BigDecimal;
import java.util.UUID;

public interface RegisterUserAssetUseCase {

    UserAsset register(
            UUID userId,
            RegisterUserAssetCommand command
    );

    record RegisterUserAssetCommand(
            String name,
            AssetType type,
            String sector,
            AssetOrigin origin,
            BigDecimal initialPrice
    ) {
    }
}