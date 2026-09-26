package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.Asset;
import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import java.time.Instant;
import java.util.List;

/** Provider-neutral boundary; a vendor adapter is selected through configuration. */
public interface MarketDataProviderPort {
    List<AssetPrice> fetchPrices(List<Asset> assets, Instant from, Instant to);
    List<FxRate> fetchFxRates(List<String> currencyPairs, Instant from, Instant to);
}
