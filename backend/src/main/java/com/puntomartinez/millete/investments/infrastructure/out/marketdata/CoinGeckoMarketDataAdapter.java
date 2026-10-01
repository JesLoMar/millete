package com.puntomartinez.millete.investments.infrastructure.out.marketdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.AssetType;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * CoinGecko historical market-chart adapter.
 *
 * CoinGecko IDs are configured explicitly by ticker.
 */
@Component
final class CoinGeckoMarketDataAdapter
        implements MarketDataSourcePort {

    private static final String PROVIDER = "COINGECKO";

    private final String apiKey;
    private final String apiKeyHeader;
    private final String baseUrl;
    private final Map<String, String> coinIds;
    private final TimeProvider time;
    private final RetryingMarketDataHttpClient http;

    CoinGeckoMarketDataAdapter(
            TimeProvider time,
            @Value("${COINGECKO_API_KEY:}") String apiKey,
            @Value("${app.investments.market-data.coingecko.base-url:https://api.coingecko.com/api/v3}")
            String baseUrl,
            @Value("${app.investments.market-data.coingecko.api-key-header:x-cg-demo-api-key}")
            String apiKeyHeader,
            @Value("${COINGECKO_ID_MAP:}")
            String configuredCoinIds,
            @Value("${app.investments.market-data.coingecko.minimum-interval:PT1S}")
            Duration minimumInterval
    ) {
        this.time = time;
        this.apiKey = apiKey == null
                ? ""
                : apiKey.trim();

        this.apiKeyHeader = apiKeyHeader;

        this.baseUrl = baseUrl
                .replaceAll("/+$", "");

        this.coinIds = parseCoinIds(
                configuredCoinIds
        );

        this.http = new RetryingMarketDataHttpClient(
                minimumInterval
        );
    }

    @Override
    public boolean supports(
            SharedAsset asset
    ) {
        return asset.getType() == AssetType.CRYPTO;
    }

    @Override
    public boolean supportsFxRates() {
        return false;
    }

    @Override
    public List<FxRate> fetchFxRates(
            List<String> currencyPairs,
            Instant from,
            Instant to
    ) {
        return List.of();
    }

    @Override
    public List<AssetPrice> fetchPrices(
            List<SharedAsset> assets,
            Instant from,
            Instant to
    ) {
        requireApiKey();

        List<AssetPrice> result =
                new ArrayList<>();

        for (SharedAsset asset : assets) {
            if (asset.getSymbol() == null
                    || asset.getSymbol().isBlank()) {
                continue;
            }

            String ticker =
                    asset.getSymbol()
                            .trim()
                            .toUpperCase(Locale.ROOT);

            String coinId =
                    coinIds.get(ticker);

            if (coinId == null) {
                throw new MarketDataProviderException(
                        "No CoinGecko ID is configured for crypto ticker "
                                + ticker
                                + "; set COINGECKO_ID_MAP using "
                                + "TICKER=coingecko-id entries"
                );
            }

            URI uri =
                    UriComponentsBuilder
                            .fromUriString(baseUrl)
                            .pathSegment(
                                    "coins",
                                    coinId,
                                    "market_chart",
                                    "range"
                            )
                            .queryParam(
                                    "vs_currency",
                                    asset.getCurrency()
                                            .value()
                                            .toLowerCase(Locale.ROOT)
                            )
                            .queryParam(
                                    "from",
                                    from.getEpochSecond()
                            )
                            .queryParam(
                                    "to",
                                    to.getEpochSecond()
                            )
                            .build()
                            .encode()
                            .toUri();

            JsonNode response =
                    http.get(
                            uri,
                            apiKeyHeader,
                            apiKey,
                            PROVIDER
                    );

            Map<Long, BigDecimal> volumes =
                    valuesByTimestamp(
                            response.path("total_volumes")
                    );

            Instant fetchedAt =
                    time.now();

            for (JsonNode point :
                    response.path("prices")) {

                if (!point.isArray()
                        || point.size() < 2
                        || !point.get(0).isNumber()
                        || !point.get(1).isNumber()) {
                    continue;
                }

                long timestampMillis =
                        point.get(0).longValue();

                Instant timestamp =
                        Instant.ofEpochMilli(
                                timestampMillis
                        );

                if (timestamp.isBefore(from)
                        || timestamp.isAfter(to)) {
                    continue;
                }

                BigDecimal close =
                        point.get(1).decimalValue();

                if (close.signum() < 0) {
                    continue;
                }

                result.add(
                        new AssetPrice(
                                UUID.randomUUID(),
                                asset.getId(),
                                timestamp,
                                null,
                                null,
                                null,
                                close,
                                null,
                                volumes.get(timestampMillis),
                                asset.getCurrency(),
                                PROVIDER,
                                fetchedAt
                        )
                );
            }
        }

        return List.copyOf(result);
    }

    private Map<Long, BigDecimal> valuesByTimestamp(
            JsonNode points
    ) {
        Map<Long, BigDecimal> result =
                new HashMap<>();

        if (!points.isArray()) {
            return result;
        }

        for (JsonNode point : points) {
            if (point.isArray()
                    && point.size() >= 2
                    && point.get(0).isNumber()
                    && point.get(1).isNumber()) {

                result.put(
                        point.get(0).longValue(),
                        point.get(1).decimalValue()
                );
            }
        }

        return result;
    }

    private Map<String, String> parseCoinIds(
            String configuration
    ) {
        Map<String, String> result =
                new HashMap<>();

        if (configuration == null
                || configuration.isBlank()) {
            return Map.of();
        }

        for (String entry :
                configuration.split("[,;]")) {

            String[] mapping =
                    entry.trim().split("=", 2);

            if (mapping.length != 2
                    || mapping[0].isBlank()
                    || mapping[1].isBlank()) {

                throw new IllegalArgumentException(
                        "COINGECKO_ID_MAP entries must use "
                                + "TICKER=coingecko-id format"
                );
            }

            result.put(
                    mapping[0]
                            .trim()
                            .toUpperCase(Locale.ROOT),
                    mapping[1]
                            .trim()
                            .toLowerCase(Locale.ROOT)
            );
        }

        return Map.copyOf(result);
    }

    private void requireApiKey() {
        if (apiKey.isBlank()) {
            throw new MarketDataProviderException(
                    "COINGECKO_API_KEY is required "
                            + "to refresh crypto prices"
            );
        }
    }
}