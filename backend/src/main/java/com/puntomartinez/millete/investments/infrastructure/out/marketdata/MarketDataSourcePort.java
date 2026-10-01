package com.puntomartinez.millete.investments.infrastructure.out.marketdata;

import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;

import java.time.Instant;
import java.util.List;

interface MarketDataSourcePort {

    boolean supports(SharedAsset asset);

    boolean supportsFxRates();

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