package com.puntomartinez.millete.investments.infrastructure.out.marketdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.puntomartinez.millete.investments.domain.model.Asset;
import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.AssetType;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/** Finnhub daily candles for non-crypto assets and historical foreign-exchange pairs. */
@Component
final class FinnhubMarketDataAdapter implements MarketDataSourcePort {
    private static final String PROVIDER = "FINNHUB";
    private final String apiKey;
    private final String baseUrl;
    private final TimeProvider time;
    private final RetryingMarketDataHttpClient http;

    FinnhubMarketDataAdapter(
            TimeProvider time,
            @Value("${FINNHUB_API_KEY:}") String apiKey,
            @Value("${app.investments.market-data.finnhub.base-url:https://finnhub.io/api/v1}") String baseUrl,
            @Value("${app.investments.market-data.finnhub.minimum-interval:PT0.25S}") Duration minimumInterval) {
        this.time = time;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.http = new RetryingMarketDataHttpClient(minimumInterval);
    }

    @Override public boolean supports(Asset asset) { return asset.getType() != AssetType.CRYPTO; }
    @Override public boolean supportsFxRates() { return true; }

    @Override
    public List<AssetPrice> fetchPrices(List<Asset> assets, Instant from, Instant to) {
        requireApiKey();
        List<AssetPrice> result = new ArrayList<>();
        for (Asset asset : assets) {
            if (asset.getSymbol() == null || asset.getSymbol().isBlank()) continue;
            URI uri = uri("stock/candle", asset.getSymbol().trim().toUpperCase(Locale.ROOT), from, to);
            JsonNode candles = http.get(uri, "X-Finnhub-Token", apiKey, PROVIDER);
            if (isNoData(candles)) continue;
            Instant fetchedAt = time.now();
            JsonNode timestamps = candles.path("t");
            for (int index = 0; index < timestamps.size(); index++) {
                Instant timestamp = Instant.ofEpochSecond(timestamps.get(index).asLong());
                if (timestamp.isBefore(from) || timestamp.isAfter(to)) continue;
                BigDecimal close = decimalAt(candles.path("c"), index);
                if (close == null) continue;
                result.add(new AssetPrice(UUID.randomUUID(), asset.getUserId(), asset.getId(), timestamp,
                        decimalAt(candles.path("o"), index), decimalAt(candles.path("h"), index),
                        decimalAt(candles.path("l"), index), close, null,
                        decimalAt(candles.path("v"), index), asset.getCurrency(), PROVIDER, fetchedAt));
            }
        }
        return List.copyOf(result);
    }

    @Override
    public List<FxRate> fetchFxRates(List<String> currencyPairs, Instant from, Instant to) {
        if (currencyPairs.isEmpty()) return List.of();
        requireApiKey();
        List<FxRate> result = new ArrayList<>();
        for (String pair : currencyPairs) {
            String[] currencies = pair.toUpperCase(Locale.ROOT).split("/");
            if (currencies.length != 2 || currencies[0].equals(currencies[1])) {
                throw new IllegalArgumentException("FX pair must use BASE/QUOTE currency codes");
            }
            String base = currencies[0];
            String quote = currencies[1];
            URI uri = uri("forex/candle", "OANDA:" + base + "_" + quote, from, to);
            JsonNode candles = http.get(uri, "X-Finnhub-Token", apiKey, PROVIDER);
            if (isNoData(candles)) continue;
            Instant fetchedAt = time.now();
            JsonNode timestamps = candles.path("t");
            for (int index = 0; index < timestamps.size(); index++) {
                Instant timestamp = Instant.ofEpochSecond(timestamps.get(index).asLong());
                if (timestamp.isBefore(from) || timestamp.isAfter(to)) continue;
                BigDecimal rate = decimalAt(candles.path("c"), index);
                if (rate != null && rate.signum() > 0) {
                    result.add(new FxRate(UUID.randomUUID(), null, base, quote, timestamp, rate, PROVIDER, fetchedAt));
                }
            }
        }
        return List.copyOf(result);
    }

    private URI uri(String endpoint, String symbol, Instant from, Instant to) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment(endpoint.split("/"))
                .queryParam("symbol", symbol)
                .queryParam("resolution", "D")
                .queryParam("from", from.getEpochSecond())
                .queryParam("to", to.getEpochSecond())
                .build().encode().toUri();
    }

    private boolean isNoData(JsonNode response) {
        String status = response.path("s").asText("");
        if ("no_data".equalsIgnoreCase(status)) return true;
        if (!"ok".equalsIgnoreCase(status)) {
            throw new MarketDataProviderException(PROVIDER + " returned an unexpected candle status");
        }
        return false;
    }

    private BigDecimal decimalAt(JsonNode values, int index) {
        if (!values.isArray() || index >= values.size() || !values.get(index).isNumber()) return null;
        return values.get(index).decimalValue();
    }

    private void requireApiKey() {
        if (apiKey.isBlank()) throw new MarketDataProviderException("FINNHUB_API_KEY is required to refresh non-crypto prices and FX rates");
    }
}
