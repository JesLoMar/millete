package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.domain.model.UserAssetPrice;
import com.puntomartinez.millete.investments.domain.ports.in.RegisterUserAssetPriceUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetPriceRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetRepository;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserAssetPriceService implements RegisterUserAssetPriceUseCase {

    private final UserAssetRepository userAssets;
    private final UserAssetPriceRepository userAssetPrices;
    private final TimeProvider time;

    public UserAssetPriceService(
            UserAssetRepository userAssets,
            UserAssetPriceRepository userAssetPrices,
            TimeProvider time
    ) {
        this.userAssets = userAssets;
        this.userAssetPrices = userAssetPrices;
        this.time = time;
    }

    @Override
    @Transactional
    public UserAssetPrice register(
            UUID userId,
            UUID userAssetId,
            RegisterUserAssetPriceCommand command
    ) {
        UserAsset userAsset = userAssets
                .findByIdAndUserId(userAssetId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "UserAsset no encontrado"
                        )
                );

        Money unitPrice = Money.of(
                command.unitPrice(),
                userAsset.getCurrency().value()
        );

        UserAssetPrice price = UserAssetPrice.create(
                time,
                userAsset.getId(),
                unitPrice
        );

        return userAssetPrices.save(price);
    }
}