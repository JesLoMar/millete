package com.puntomartinez.millete.investments.infrastructure.out.marketdata;

import com.puntomartinez.millete.investments.domain.model.Asset;
import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import java.time.Instant;
import java.util.List;

/** Infrastructure boundary implemented by each external market-data vendor. */
interface MarketDataSourcePort {
    boolean supports(Asset asset);
    boolean supportsFxRates();
    List<AssetPrice> fetchPrices(List<Asset> assets, Instant from, Instant to);
    List<FxRate> fetchFxRates(List<String> currencyPairs, Instant from, Instant to);
}
