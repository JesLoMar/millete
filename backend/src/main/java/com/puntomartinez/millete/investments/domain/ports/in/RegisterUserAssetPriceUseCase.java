package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.UserAssetPrice;

import java.math.BigDecimal;
import java.util.UUID;

public interface RegisterUserAssetPriceUseCase {

    UserAssetPrice register(UUID userId,
                            UUID userAssetId,
                            RegisterUserAssetPriceCommand command);

    record RegisterUserAssetPriceCommand(
            BigDecimal unitPrice
    ) {
    }
}