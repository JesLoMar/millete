package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.domain.ports.in.RefreshMarketDataUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.AssetPriceRepository;
import com.puntomartinez.millete.investments.domain.ports.out.FxRateRepository;
import com.puntomartinez.millete.investments.domain.ports.out.MarketDataProviderPort;
import com.puntomartinez.millete.investments.domain.ports.out.SharedAssetRepository;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Refresco global de market data.
 *
 * El rango de cada serie se calcula de forma automática: desde la última
 * entrada guardada hasta {@code now}. Si no hay datos previos se usa el
 * {@code from} recibido o una ventana por defecto.
 */
@Service
public class MarketDataRefreshService
        implements RefreshMarketDataUseCase {

    private static final Duration DEFAULT_LOOKBACK =
            Duration.ofDays(30);

    private static final Duration MAX_LOOKBACK =
            Duration.ofDays(365);

    private final SharedAssetRepository sharedAssetRepository;
    private final MarketDataProviderPort marketDataProvider;
    private final AssetPriceRepository assetPriceRepository;
    private final FxRateRepository fxRateRepository;
    private final TimeProvider time;

    public MarketDataRefreshService(
            SharedAssetRepository sharedAssetRepository,
            MarketDataProviderPort marketDataProvider,
            AssetPriceRepository assetPriceRepository,
            FxRateRepository fxRateRepository,
            TimeProvider time
    ) {
        this.sharedAssetRepository = sharedAssetRepository;
        this.marketDataProvider = marketDataProvider;
        this.assetPriceRepository = assetPriceRepository;
        this.fxRateRepository = fxRateRepository;
        this.time = time;
    }

    @Override
    public void refresh(
            RefreshMarketDataCommand command
    ) {
        validateCommand(command);

        Instant to = resolveTo(command);
        Instant fallbackFrom = resolveFallbackFrom(
                command.from(),
                to
        );

        refreshPrices(
                command,
                to,
                fallbackFrom
        );

        refreshFxRates(
                command,
                to,
                fallbackFrom
        );
    }

    private void refreshPrices(
            RefreshMarketDataCommand command,
            Instant to,
            Instant fallbackFrom
    ) {
        List<SharedAsset> assets =
                sharedAssetRepository.findAll();

        if (assets.isEmpty()) {
            return;
        }

        List<AssetPrice> prices =
                new ArrayList<>();

        for (SharedAsset asset : assets) {
            Instant from = resolveSeriesFrom(
                    assetPriceRepository
                            .findLatestAt(asset.getId(), to)
                            .map(AssetPrice::timestamp)
                            .orElse(null),
                    command.from(),
                    fallbackFrom,
                    to
            );

            if (from == null) {
                continue;
            }

            prices.addAll(
                    marketDataProvider.fetchPrices(
                            List.of(asset),
                            from,
                            to
                    )
            );
        }

        validatePrices(
                prices,
                assets
        );

        if (!prices.isEmpty()) {
            assetPriceRepository.saveAll(prices);
        }
    }

    private void refreshFxRates(
            RefreshMarketDataCommand command,
            Instant to,
            Instant fallbackFrom
    ) {
        if (command.currencyPairs().isEmpty()) {
            return;
        }

        List<FxRate> fxRates =
                new ArrayList<>();

        for (String pair : command.currencyPairs()) {
            String[] currencies = parsePair(pair);
            CurrencyCode base = CurrencyCode.of(currencies[0]);
            CurrencyCode quote = CurrencyCode.of(currencies[1]);

            Instant from = resolveSeriesFrom(
                    fxRateRepository
                            .findLatestAt(base, quote, to)
                            .map(FxRate::timestamp)
                            .orElse(null),
                    command.from(),
                    fallbackFrom,
                    to
            );

            if (from == null) {
                continue;
            }

            fxRates.addAll(
                    marketDataProvider.fetchFxRates(
                            List.of(pair),
                            from,
                            to
                    )
            );
        }

        validateFxRates(fxRates);

        if (!fxRates.isEmpty()) {
            fxRateRepository.saveAll(fxRates);
        }
    }

    private Instant resolveTo(
            RefreshMarketDataCommand command
    ) {
        Instant now = time.now();
        Instant requestedTo = command.to();

        if (requestedTo != null
                && requestedTo.isBefore(now)) {
            return requestedTo;
        }

        return now;
    }

    private Instant resolveFallbackFrom(
            Instant requestedFrom,
            Instant to
    ) {
        if (requestedFrom != null) {
            return requestedFrom;
        }

        return to.minus(
                DEFAULT_LOOKBACK
        );
    }

    private Instant resolveSeriesFrom(
            Instant lastStored,
            Instant requestedFrom,
            Instant fallbackFrom,
            Instant to
    ) {
        Instant candidate =
                lastStored != null
                        ? lastStored
                        : fallbackFrom;

        if (requestedFrom != null
                && requestedFrom.isAfter(candidate)) {
            candidate = requestedFrom;
        }

        Instant earliest =
                to.minus(
                        MAX_LOOKBACK
                );

        if (candidate.isBefore(earliest)) {
            candidate = earliest;
        }

        return candidate.isBefore(to)
                ? candidate
                : null;
    }

    private String[] parsePair(
            String pair
    ) {
        String[] currencies =
                pair == null
                        ? new String[0]
                        : pair.trim()
                                .toUpperCase(Locale.ROOT)
                                .split("/");

        if (currencies.length != 2
                || currencies[0].length() != 3
                || currencies[1].length() != 3
                || currencies[0].equals(currencies[1])) {

            throw new InvalidInputException(
                    "el par debe usar el formato BASE/QUOTE "
                            + "con monedas distintas"
            );
        }

        return currencies;
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

        if (from != null
                && to != null
                && !from.isBefore(to)) {
            throw new InvalidInputException(
                    "from debe ser anterior a to."
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
