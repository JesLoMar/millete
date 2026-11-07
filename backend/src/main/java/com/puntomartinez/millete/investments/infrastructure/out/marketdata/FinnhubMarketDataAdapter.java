package com.puntomartinez.millete.investments.infrastructure.out.marketdata;

import tools.jackson.databind.JsonNode;
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
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Velas diarias de acciones y ETFs desde Finnhub.
 *
 * Los tipos de cambio ya no se piden a Finnhub: su endpoint {@code forex/candle}
 * es de pago y devuelve 403 con el plan gratuito. Los sirve
 * {@link YahooFinanceMarketDataAdapter}.
 */
@Component
final class FinnhubMarketDataAdapter
        implements MarketDataSourcePort {

    private static final String PROVIDER = "FINNHUB";

    private final String apiKey;
    private final String baseUrl;
    private final TimeProvider time;
    private final RetryingMarketDataHttpClient http;

    FinnhubMarketDataAdapter(
            TimeProvider time,
            @Value("${FINNHUB_API_KEY:}") String apiKey,
            @Value("${app.investments.market-data.finnhub.base-url:https://finnhub.io/api/v1}")
            String baseUrl,
            @Value("${app.investments.market-data.finnhub.minimum-interval:PT0.25S}")
            Duration minimumInterval
    ) {
        this.time = time;

        this.apiKey = apiKey == null
                ? ""
                : apiKey.trim();

        this.baseUrl = baseUrl
                .replaceAll("/+$", "");

        this.http = new RetryingMarketDataHttpClient(
                minimumInterval
        );
    }

       @Override
   public boolean supports(SharedAsset asset) {
       return false;
   }

    @Override
    public boolean supportsFxRates() {
        return false;
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

            URI uri =
                    uri(
                            "stock/candle",
                            asset.getSymbol()
                                    .trim()
                                    .toUpperCase(Locale.ROOT),
                            from,
                            to
                    );

            JsonNode candles =
                    http.get(
                            uri,
                            "X-Finnhub-Token",
                            apiKey,
                            PROVIDER
                    );

            if (isNoData(candles)) {
                continue;
            }

            Instant fetchedAt =
                    time.now();

            JsonNode timestamps =
                    candles.path("t");

            for (
                    int index = 0;
                    index < timestamps.size();
                    index++
            ) {
                Instant timestamp =
                        Instant.ofEpochSecond(
                                timestamps
                                        .get(index)
                                        .asLong()
                        );

                if (timestamp.isBefore(from)
                        || timestamp.isAfter(to)) {
                    continue;
                }

                BigDecimal close =
                        decimalAt(
                                candles.path("c"),
                                index
                        );

                if (close == null) {
                    continue;
                }

                result.add(
                        new AssetPrice(
                                UUID.randomUUID(),
                                asset.getId(),
                                timestamp,
                                decimalAt(
                                        candles.path("o"),
                                        index
                                ),
                                decimalAt(
                                        candles.path("h"),
                                        index
                                ),
                                decimalAt(
                                        candles.path("l"),
                                        index
                                ),
                                close,
                                null,
                                decimalAt(
                                        candles.path("v"),
                                        index
                                ),
                                asset.getCurrency(),
                                PROVIDER,
                                fetchedAt
                        )
                );
            }
        }

        return List.copyOf(result);
    }

    /**
     * Finnhub no es fuente de tipos de cambio: {@code forex/candle} requiere un
     * plan de pago. El proveedor de FX es Yahoo Finance.
     */
    @Override
    public List<FxRate> fetchFxRates(
            List<String> currencyPairs,
            Instant from,
            Instant to
    ) {
        return List.of();
    }

    private URI uri(
            String endpoint,
            String symbol,
            Instant from,
            Instant to
    ) {
        return UriComponentsBuilder
                .fromUriString(baseUrl)
                .pathSegment(
                        endpoint.split("/")
                )
                .queryParam(
                        "symbol",
                        symbol
                )
                .queryParam(
                        "resolution",
                        "D"
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
    }

    private boolean isNoData(
            JsonNode response
    ) {
        String status =
                response.path("s").asText("");

        if ("no_data".equalsIgnoreCase(status)) {
            return true;
        }

        if (!"ok".equalsIgnoreCase(status)) {
            throw new MarketDataProviderException(
                    PROVIDER
                            + " returned an unexpected candle status"
            );
        }

        return false;
    }

    private BigDecimal decimalAt(
            JsonNode values,
            int index
    ) {
        if (!values.isArray()
                || index >= values.size()
                || !values.get(index).isNumber()) {
            return null;
        }

        return values
                .get(index)
                .decimalValue();
    }

    private void requireApiKey() {
        if (apiKey.isBlank()) {
            throw new MarketDataProviderException(
                    "FINNHUB_API_KEY is required to refresh "
                            + "stock and ETF prices"
            );
        }
    }
}