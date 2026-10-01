package com.puntomartinez.millete.investments.application.service;

import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.domain.ports.in.RefreshMarketDataUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.AssetPriceRepository;
import com.puntomartinez.millete.investments.domain.ports.out.FxRateRepository;
import com.puntomartinez.millete.investments.domain.ports.out.MarketDataProviderPort;
import com.puntomartinez.millete.investments.domain.ports.out.SharedAssetRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MarketDataRefreshService
        implements RefreshMarketDataUseCase {

    private final SharedAssetRepository sharedAssetRepository;
    private final MarketDataProviderPort marketDataProvider;
    private final AssetPriceRepository assetPriceRepository;
    private final FxRateRepository fxRateRepository;

    public MarketDataRefreshService(
            SharedAssetRepository sharedAssetRepository,
            MarketDataProviderPort marketDataProvider,
            AssetPriceRepository assetPriceRepository,
            FxRateRepository fxRateRepository
    ) {
        this.sharedAssetRepository = sharedAssetRepository;
        this.marketDataProvider = marketDataProvider;
        this.assetPriceRepository = assetPriceRepository;
        this.fxRateRepository = fxRateRepository;
    }

    @Override
    public void refresh(
            RefreshMarketDataCommand command
    ) {
        validateCommand(command);

        List<SharedAsset> assets =
                sharedAssetRepository.findAll();

        if (!assets.isEmpty()) {
            List<AssetPrice> prices =
                    marketDataProvider.fetchPrices(
                            assets,
                            command.from(),
                            command.to()
                    );

            validatePrices(
                    prices,
                    assets
            );

            if (!prices.isEmpty()) {
                assetPriceRepository.saveAll(prices);
            }
        }

        if (!command.currencyPairs().isEmpty()) {
            List<FxRate> fxRates =
                    marketDataProvider.fetchFxRates(
                            command.currencyPairs(),
                            command.from(),
                            command.to()
                    );

            validateFxRates(fxRates);

            if (!fxRates.isEmpty()) {
                fxRateRepository.saveAll(fxRates);
            }
        }
    }

    private void validateCommand(
            RefreshMarketDataCommand command
    ) {
        if (command == null) {
            throw new IllegalArgumentException(
                    "El command de refresh es obligatorio"
            );
        }

        Instant from = command.from();
        Instant to = command.to();

        if (from == null) {
            throw new IllegalArgumentException(
                    "from es obligatorio"
            );
        }

        if (to == null) {
            throw new IllegalArgumentException(
                    "to es obligatorio"
            );
        }

        if (!from.isBefore(to)) {
            throw new IllegalArgumentException(
                    "from debe ser anterior a to"
            );
        }

        if (command.currencyPairs() == null) {
            throw new IllegalArgumentException(
                    "currencyPairs es obligatorio"
            );
        }
    }

    private void validatePrices(
            List<AssetPrice> prices,
            List<SharedAsset> assets
    ) {
        if (prices == null) {
            throw new IllegalStateException(
                    "El proveedor de Market Data ha devuelto null en los precios"
            );
        }

        Map<UUID, SharedAsset> assetsById =
                new HashMap<>();

        for (SharedAsset asset : assets) {
            assetsById.put(
                    asset.getId(),
                    asset
            );
        }

        for (AssetPrice price : prices) {
            SharedAsset asset =
                    assetsById.get(
                            price.sharedAssetId()
                    );

            if (asset == null) {
                throw new IllegalStateException(
                        "El proveedor ha devuelto un precio " +
                                "para un SharedAsset que no pertenece " +
                                "al catálogo solicitado: " +
                                price.sharedAssetId()
                );
            }

            if (!asset.getCurrency().equals(
                    price.currency()
            )) {
                throw new IllegalStateException(
                        "La moneda del precio no coincide con " +
                                "la moneda del SharedAsset " +
                                asset.getId()
                );
            }
        }
    }

    private void validateFxRates(
            List<FxRate> fxRates
    ) {
        if (fxRates == null) {
            throw new IllegalStateException(
                    "El proveedor de Market Data ha devuelto null en los FX"
            );
        }
    }
}