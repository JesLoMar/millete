package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarketDataRepository {
    void saveMarketData(List<AssetPrice> prices, List<FxRate> rates);
    AssetPrice savePrice(AssetPrice price);
    FxRate saveFxRate(FxRate rate);
    Optional<AssetPrice> latestPriceAt(UUID userId, UUID assetId, Instant at);
    Optional<FxRate> latestFxAt(UUID userId, String baseCurrency, String quoteCurrency, Instant at);
    List<AssetPrice> pricesForAsset(UUID userId, UUID assetId, Instant from, Instant to);
    List<FxRate> fxRates(UUID userId, Instant from, Instant to);
}
