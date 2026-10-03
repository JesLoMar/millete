package com.puntomartinez.millete.investments.infrastructure.out.marketdata;

import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.domain.ports.out.MarketDataProviderPort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public final class CompositeMarketDataProviderAdapter
        implements MarketDataProviderPort {

    private final List<MarketDataSourcePort> sources;

    public CompositeMarketDataProviderAdapter(
            List<MarketDataSourcePort> sources
    ) {
        this.sources = List.copyOf(sources);
    }

    @Override
    public List<AssetPrice> fetchPrices(
            List<SharedAsset> assets,
            Instant from,
            Instant to
    ) {
        List<AssetPrice> result = new ArrayList<>();

        for (SharedAsset asset : assets) {
            List<MarketDataSourcePort> matching =
                    sources.stream()
                            .filter(source -> source.supports(asset))
                            .toList();

            if (matching.size() != 1) {
                throw new IllegalStateException(
                        "Expected exactly one market-data provider "
                                + "for SharedAsset "
                                + asset.getId()
                );
            }

            result.addAll(
                    matching.getFirst()
                            .fetchPrices(
                                    List.of(asset),
                                    from,
                                    to
                            )
            );
        }

        return List.copyOf(result);
    }

    @Override
    public List<FxRate> fetchFxRates(
            List<String> currencyPairs,
            Instant from,
            Instant to
    ) {
        if (currencyPairs.isEmpty()) {
            return List.of();
        }

        List<MarketDataSourcePort> fxSources =
                sources.stream()
                        .filter(MarketDataSourcePort::supportsFxRates)
                        .toList();

        if (fxSources.size() != 1) {
            throw new IllegalStateException(
                    "Exactly one FX market-data provider "
                            + "must be configured"
            );
        }

        return fxSources
                .getFirst()
                .fetchFxRates(
                        currencyPairs,
                        from,
                        to
                );
    }
}