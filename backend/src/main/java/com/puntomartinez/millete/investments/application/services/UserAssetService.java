package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.AssetSector;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.domain.model.UserAssetPrice;
import com.puntomartinez.millete.investments.domain.ports.in.CreateHoldingUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.GetUserAssetUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.ListUserAssetsUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RegisterUserAssetUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.UpdateUserAssetUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.AssetSectorCatalogPort;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentPortfolioLockPort;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetPriceRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserCurrencyPort;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class UserAssetService implements
        RegisterUserAssetUseCase,
        UpdateUserAssetUseCase,
        GetUserAssetUseCase,
        ListUserAssetsUseCase {

    private final UserAssetRepository userAssets;
    private final UserAssetPriceRepository userAssetPrices;
    private final AssetSectorCatalogPort sectorCatalog;
    private final UserCurrencyPort userCurrencies;
    private final CreateHoldingUseCase createHolding;
    private final InvestmentPortfolioLockPort portfolioLock;
    private final TimeProvider time;

    public UserAssetService(
            UserAssetRepository userAssets,
            UserAssetPriceRepository userAssetPrices,
            AssetSectorCatalogPort sectorCatalog,
            UserCurrencyPort userCurrencies,
            CreateHoldingUseCase createHolding,
            InvestmentPortfolioLockPort portfolioLock,
            TimeProvider time
    ) {
        this.userAssets = userAssets;
        this.userAssetPrices = userAssetPrices;
        this.sectorCatalog = sectorCatalog;
        this.userCurrencies = userCurrencies;
        this.createHolding = createHolding;
        this.portfolioLock = portfolioLock;
        this.time = time;
    }

    @Override
    @Transactional
    public UserAsset register(
            UUID userId,
            RegisterUserAssetCommand command
    ) {
        var openingPosition = command.openingPosition();
        if (openingPosition != null) {
            portfolioLock.lock(userId);
        }

        Instant now = time.now();

        CurrencyCode currency = userCurrencies
                .currencyAt(userId, now)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No hay una moneda local configurada para el usuario"
                        )
                );

        AssetSector sector = resolveSector(command.sector());

        UserAsset userAsset = UserAsset.create(
                time,
                userId,
                command.name(),
                command.type(),
                sector,
                command.origin(),
                currency
        );

        UserAsset savedAsset = userAssets.save(userAsset);

        UserAssetPrice initialPrice = UserAssetPrice.create(
                time,
                savedAsset.getId(),
                Money.of(
                        command.initialPrice(),
                        currency.value()
                )
        );

        userAssetPrices.save(initialPrice);

        if (openingPosition != null) {
            createHolding.create(userId, new CreateHoldingUseCase.CreateHoldingCommand(
                    AssetReference.user(savedAsset.getId()),
                    initialPrice.getTimestamp(),
                    openingPosition.quantity(),
                    openingPosition.acquisitionCost()
            ));
        }

        return savedAsset;
    }

    @Override
    @Transactional
    public UserAsset update(
            UUID userId,
            UUID userAssetId,
            UpdateUserAssetCommand command
    ) {
        UserAsset userAsset = getOwnedAsset(
                userId,
                userAssetId
        );

        AssetSector sector = resolveSector(command.sector());

        userAsset.updateDetails(
                time,
                command.name(),
                sector
        );

        return userAssets.save(userAsset);
    }

    @Override
    @Transactional(readOnly = true)
    public UserAsset getById(
            UUID userId,
            UUID userAssetId
    ) {
        return getOwnedAsset(
                userId,
                userAssetId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAsset> list(
            UUID userId,
            String search
    ) {
        String normalizedSearch = normalizeSearch(search);

        return userAssets.findAllByUserId(
                userId,
                normalizedSearch
        );
    }

    private UserAsset getOwnedAsset(
            UUID userId,
            UUID userAssetId
    ) {
        return userAssets
                .findByIdAndUserId(userAssetId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "UserAsset no encontrado"
                        )
                );
    }

    private AssetSector resolveSector(String sector) {
        String normalizedSector = normalizeSector(sector);

        return sectorCatalog
                .findCommonByReference(normalizedSector)
                .orElseGet(() ->
                        AssetSector.custom(normalizedSector)
                );
    }

    private String normalizeSector(String sector) {
        if (sector == null || sector.isBlank()) {
            throw new IllegalArgumentException(
                    "El sector es obligatorio"
            );
        }

        return sector.trim();
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }

        return search.trim();
    }
}
