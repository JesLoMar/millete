package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarketDataRepository {
    AssetPrice savePrice(AssetPrice price);
    FxRate saveFxRate(FxRate rate);
    Optional<AssetPrice> latestPriceAt(UUID userId, UUID assetId, Instant at);
    Optional<FxRate> latestFxAt(String baseCurrency, String quoteCurrency, Instant at);
    List<AssetPrice> pricesForAsset(UUID userId, UUID assetId, Instant from, Instant to);
    List<FxRate> fxRates(Instant from, Instant to);
}
