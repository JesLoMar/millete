package com.puntomartinez.millete.investments.infrastructure.out.marketdata;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** Sequential rate limiting and bounded retries for transient upstream failures. */
final class RetryingMarketDataHttpClient {
    private static final int MAX_ATTEMPTS = 3;
    private final RestClient client = RestClient.create();
    private final long minimumIntervalNanos;
    private final AtomicLong nextRequestNanos = new AtomicLong();

    RetryingMarketDataHttpClient(Duration minimumInterval) {
        this.minimumIntervalNanos = Math.max(0L, minimumInterval.toNanos());
    }

    JsonNode get(URI uri, String headerName, String headerValue, String providerName) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            waitForRateLimit();
            try {
                JsonNode response = client.get().uri(uri)
                        .header(headerName, headerValue)
                        .retrieve().body(JsonNode.class);
                if (response == null || !response.isObject() && !response.isArray()) {
                    throw new MarketDataProviderException(providerName + " returned an empty or invalid response for " + uri.getPath());
                }
                return response;
            } catch (RestClientResponseException exception) {
                int status = exception.getStatusCode().value();
                lastFailure = new MarketDataProviderException(providerName + " returned HTTP " + status + " for " + uri.getPath());
                if (status != 429 && status < 500) throw lastFailure;
            } catch (ResourceAccessException exception) {
                lastFailure = new MarketDataProviderException(providerName + " could not be reached for " + uri.getPath(), exception);
            }
            if (attempt < MAX_ATTEMPTS) pause(Duration.ofMillis(250L * attempt));
        }
        throw lastFailure == null
                ? new MarketDataProviderException(providerName + " request failed for " + uri.getPath())
                : lastFailure;
    }

    private void waitForRateLimit() {
        long now = System.nanoTime();
        long scheduled = nextRequestNanos.getAndUpdate(previous -> Math.max(now, previous) + minimumIntervalNanos);
        if (scheduled > now) pause(Duration.ofNanos(scheduled - now));
    }

    private void pause(Duration duration) {
        try {
            long millis = duration.toMillis();
            int nanos = (int) (duration.minusMillis(millis).toNanos());
            Thread.sleep(millis, nanos);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new MarketDataProviderException("Market-data request was interrupted", exception);
        }
    }
}
