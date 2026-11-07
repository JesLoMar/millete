package com.puntomartinez.millete.investments.infrastructure.out.marketdata;

import tools.jackson.databind.JsonNode;
import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.AssetType;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
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
 * Precio de cierre diario de acciones, ETFs y tipos de cambio desde Yahoo
 * Finance (endpoint chart, sin API key).
 *
 * Los pares de divisas se consultan con el símbolo {@code BASEQUOTE=X}, que
 * Yahoo resuelve como el par directo (por ejemplo, {@code EURUSD=X}). Es un
 * endpoint no oficial: puede cambiar sin aviso.
 */
@Component
final class YahooFinanceMarketDataAdapter
        implements MarketDataSourcePort {

    private static final String PROVIDER = "YAHOO";
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String FX_SYMBOL_SUFFIX = "=X";

    private final String baseUrl;
    private final TimeProvider time;
    private final RetryingMarketDataHttpClient http;

    YahooFinanceMarketDataAdapter(
            TimeProvider time,
            @Value("${app.investments.market-data.yahoo.base-url:https://query1.finance.yahoo.com}")
            String baseUrl,
            @Value("${app.investments.market-data.yahoo.minimum-interval:PT1S}")
            Duration minimumInterval
    ) {
        this.time = time;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.http = new RetryingMarketDataHttpClient(minimumInterval);
    }

    @Override
    public boolean supports(SharedAsset asset) {
        return asset.getType() == AssetType.STOCK
                || asset.getType() == AssetType.ETF;
    }

    @Override
    public boolean supportsFxRates() {
        return true;
    }

    @Override
    public List<FxRate> fetchFxRates(
            List<String> currencyPairs,
            Instant from,
            Instant to
    ) {
        List<FxRate> result = new ArrayList<>();

        for (String pair : currencyPairs) {
            String[] currencies = pair == null
                    ? new String[0]
                    : pair.trim()
                            .toUpperCase(Locale.ROOT)
                            .split("/");

            if (currencies.length != 2
                    || currencies[0].length() != 3
                    || currencies[1].length() != 3
                    || currencies[0].equals(currencies[1])) {
                throw new IllegalArgumentException(
                        "El par de divisas debe usar el formato BASE/QUOTE"
                );
            }

            String base = currencies[0];
            String quote = currencies[1];

            JsonNode data = chart(
                    base + quote + FX_SYMBOL_SUFFIX,
                    from,
                    to
            );

            JsonNode timestamps = data.path("timestamp");
            JsonNode closes = data
                    .path("indicators")
                    .path("quote")
                    .path(0)
                    .path("close");

            Instant fetchedAt = time.now();

            for (int index = 0; index < timestamps.size(); index++) {
                Instant timestamp = Instant.ofEpochSecond(
                        timestamps.get(index).asLong()
                );

                if (timestamp.isBefore(from) || timestamp.isAfter(to)) {
                    continue;
                }

                BigDecimal rate = decimalAt(closes, index);

                if (rate == null || rate.signum() <= 0) {
                    continue;
                }

                result.add(
                        new FxRate(
                                UUID.randomUUID(),
                                CurrencyCode.of(base),
                                CurrencyCode.of(quote),
                                timestamp,
                                rate,
                                PROVIDER,
                                fetchedAt
                        )
                );
            }
        }

        return List.copyOf(result);
    }

    @Override
    public List<AssetPrice> fetchPrices(
            List<SharedAsset> assets,
            Instant from,
            Instant to
    ) {
        List<AssetPrice> result = new ArrayList<>();

        for (SharedAsset asset : assets) {
            if (asset.getSymbol() == null || asset.getSymbol().isBlank()) {
                continue;
            }

            JsonNode data = chart(
                    asset.getSymbol().trim(),
                    from,
                    to
            );

            JsonNode timestamps = data.path("timestamp");
            JsonNode quote = data
                    .path("indicators")
                    .path("quote")
                    .path(0);
            JsonNode closes = quote.path("close");
            JsonNode volumes = quote.path("volume");

            Instant fetchedAt = time.now();

            for (int index = 0; index < timestamps.size(); index++) {
                Instant timestamp = Instant.ofEpochSecond(
                        timestamps.get(index).asLong()
                );

                if (timestamp.isBefore(from) || timestamp.isAfter(to)) {
                    continue;
                }

                BigDecimal close = decimalAt(closes, index);

                if (close == null) {
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
                                decimalAt(volumes, index),
                                asset.getCurrency(),
                                PROVIDER,
                                fetchedAt
                        )
                );
            }
        }

        return List.copyOf(result);
    }

    private JsonNode chart(
            String symbol,
            Instant from,
            Instant to
    ) {
        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl)
                .pathSegment("v8", "finance", "chart", symbol)
                .queryParam("period1", from.getEpochSecond())
                .queryParam("period2", to.getEpochSecond())
                .queryParam("interval", "1d")
                .build()
                .encode()
                .toUri();

        JsonNode chart = http.get(uri, "User-Agent", USER_AGENT, PROVIDER)
                .path("chart");

        if (chart.hasNonNull("error")) {
            throw new MarketDataProviderException(
                    PROVIDER + " error for " + symbol
                            + ": " + chart.path("error").path("description").asText("unknown")
            );
        }

        return chart.path("result").path(0);
    }

    private BigDecimal decimalAt(JsonNode values, int index) {
        if (!values.isArray()
                || index >= values.size()
                || !values.get(index).isNumber()) {
            return null;
        }

        return values.get(index).decimalValue();
    }
}
