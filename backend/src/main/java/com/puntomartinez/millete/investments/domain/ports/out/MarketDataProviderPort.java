package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;

import java.time.Instant;
import java.util.List;

public interface MarketDataProviderPort {

    String providerName();

    List<AssetPrice> fetchPrices(
            List<SharedAsset> assets,
            Instant from,
            Instant to
    );

    List<FxRate> fetchFxRates(
            List<String> currencyPairs,
            Instant from,
            Instant to
    );
}