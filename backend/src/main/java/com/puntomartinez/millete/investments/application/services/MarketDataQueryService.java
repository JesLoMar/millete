package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.domain.ports.in.ListAssetPricesUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.ListFxRatesUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.AssetPriceRepository;
import com.puntomartinez.millete.investments.domain.ports.out.FxRateRepository;
import com.puntomartinez.millete.investments.domain.ports.out.SharedAssetRepository;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class MarketDataQueryService implements
        ListAssetPricesUseCase,
        ListFxRatesUseCase {

    private final AssetPriceRepository assetPrices;
    private final FxRateRepository fxRates;
    private final SharedAssetRepository sharedAssets;

    public MarketDataQueryService(
            AssetPriceRepository assetPrices,
            FxRateRepository fxRates,
            SharedAssetRepository sharedAssets
    ) {
        this.assetPrices = assetPrices;
        this.fxRates = fxRates;
        this.sharedAssets = sharedAssets;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetPrice> list(
            UUID sharedAssetId,
            Instant from,
            Instant to
    ) {
        validateSharedAssetId(sharedAssetId);
        validatePeriod(from, to);

        sharedAssets.findById(sharedAssetId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "SharedAsset no encontrado."
                        )
                );

        return assetPrices
                .findBySharedAssetIdAndTimestampBetween(
                        sharedAssetId,
                        from,
                        to
                )
                .stream()
                .sorted(
                        Comparator.comparing(
                                AssetPrice::timestamp
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FxRate> list(
            String baseCurrency,
            String quoteCurrency,
            Instant from,
            Instant to
    ) {
        validatePeriod(from, to);

        CurrencyCode base =
                parseCurrency(baseCurrency);

        CurrencyCode quote =
                parseCurrency(quoteCurrency);

        if (base.equals(quote)) {
            throw new IllegalArgumentException(
                    "La moneda base y la moneda destino deben ser distintas."
            );
        }

        return fxRates
                .findByCurrenciesAndTimestampBetween(
                        base,
                        quote,
                        from,
                        to
                )
                .stream()
                .sorted(
                        Comparator.comparing(
                                FxRate::timestamp
                        )
                )
                .toList();
    }

    private CurrencyCode parseCurrency(
            String currency
    ) {
        if (currency == null
                || currency.isBlank()) {

            throw new IllegalArgumentException(
                    "La moneda es obligatoria."
            );
        }

        return CurrencyCode.of(
                currency.trim()
        );
    }

    private void validateSharedAssetId(
            UUID sharedAssetId
    ) {
        if (sharedAssetId == null) {
            throw new IllegalArgumentException(
                    "El SharedAsset es obligatorio."
            );
        }
    }

    private void validatePeriod(
            Instant from,
            Instant to
    ) {
        if (from == null
                || to == null) {

            throw new IllegalArgumentException(
                    "from y to son obligatorios."
            );
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "from no puede ser posterior a to."
            );
        }
    }
}