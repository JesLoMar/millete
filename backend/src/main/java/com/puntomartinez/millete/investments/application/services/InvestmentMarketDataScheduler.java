package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.ports.in.RefreshMarketDataUseCase;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Component
public final class InvestmentMarketDataScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    InvestmentMarketDataScheduler.class
            );

    private static final Duration REFRESH_WINDOW =
            Duration.ofDays(5);

    private final RefreshMarketDataUseCase refreshMarketData;
    private final TimeProvider time;
    private final List<String> currencyPairs;

    public InvestmentMarketDataScheduler(
            RefreshMarketDataUseCase refreshMarketData,
            TimeProvider time,
            @Value("${app.investments.market-data.currency-pairs:}")
            String currencyPairsConfig
    ) {
        this.refreshMarketData = refreshMarketData;
        this.time = time;
        this.currencyPairs =
                parseCurrencyPairs(
                        currencyPairsConfig
                );
    }

    @Scheduled(
            fixedDelayString =
                    "${app.investments.market-data.refresh-interval-ms:21600000}",
            initialDelayString =
                    "${app.investments.market-data.initial-delay-ms:60000}"
    )
    public void refreshMarketData() {
        Instant to =
                time.now();

        Instant from =
                to.minus(
                        REFRESH_WINDOW
                );

        RefreshMarketDataUseCase.RefreshMarketDataCommand command =
                new RefreshMarketDataUseCase.RefreshMarketDataCommand(
                        from,
                        to,
                        currencyPairs
                );

        try {
            refreshMarketData.refresh(
                    command
            );

            log.debug(
                    "Investment market data refreshed successfully "
                            + "from {} to {}",
                    from,
                    to
            );

        } catch (RuntimeException exception) {
            log.error(
                    "Scheduled investment market-data refresh failed "
                            + "from {} to {}",
                    from,
                    to,
                    exception
            );
        }
    }

    private List<String> parseCurrencyPairs(
            String currencyPairsConfig
    ) {
        if (currencyPairsConfig == null
                || currencyPairsConfig.isBlank()) {
            return List.of();
        }

        return Arrays.stream(
                        currencyPairsConfig.split(",")
                )
                .map(String::trim)
                .filter(pair -> !pair.isBlank())
                .toList();
    }
}