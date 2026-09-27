package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.ports.in.InvestmentUseCases;
import com.puntomartinez.millete.investments.domain.ports.out.AssetRepository;
import com.puntomartinez.millete.investments.infrastructure.out.marketdata.MarketDataProviderException;
import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.domain.ports.out.NotificationRepository;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodically refreshes a short overlapping window so late daily candles are picked up. */
@Component
public final class InvestmentMarketDataScheduler {
    private static final Logger log = LoggerFactory.getLogger(InvestmentMarketDataScheduler.class);
    private static final Duration REFRESH_WINDOW = Duration.ofDays(5);
    private static final String FAILURE_KEY = "investments-market-data-refresh";

    private final AssetRepository assets;
    private final InvestmentUseCases investments;
    private final TimeProvider time;
    private final NotificationRepository notifications;

    public InvestmentMarketDataScheduler(AssetRepository assets,
                                         InvestmentUseCases investments,
                                         TimeProvider time,
                                         NotificationRepository notifications) {
        this.assets = assets;
        this.investments = investments;
        this.time = time;
        this.notifications = notifications;
    }

    @Scheduled(fixedDelayString = "${app.investments.market-data.refresh-interval-ms:21600000}",
            initialDelayString = "${app.investments.market-data.initial-delay-ms:60000}")
    public void refreshActivePortfolios() {
        Instant to = time.now();
        Instant from = to.minus(REFRESH_WINDOW);
        for (UUID userId : assets.findUserIdsWithActiveAssets()) {
            try {
                investments.refreshFromProvider(userId, from, to);
                clearFailure(userId);
            } catch (RuntimeException exception) {
                log.error("Scheduled investment market-data refresh failed for user {}", userId, exception);
                recordFailure(userId, exception);
            }
        }
    }

    private void recordFailure(UUID userId, RuntimeException exception) {
        String key = FAILURE_KEY + ":" + userId;
        if (notifications.findActiveByUserIdAndTypeAndMetadataValue(
                userId, NotificationType.SYSTEM, "refreshKey", key).isPresent()) return;
        String reason = exception instanceof MarketDataProviderException
                ? exception.getMessage() : "Comprueba la configuración del proveedor y vuelve a intentarlo.";
        if (reason != null && reason.length() > 300) reason = reason.substring(0, 300);
        notifications.save(Notification.create(time, userId, NotificationType.SYSTEM,
                "No se pudieron actualizar los datos de mercado", reason,
                java.util.Map.of("source", FAILURE_KEY, "refreshKey", key), false, null));
    }

    private void clearFailure(UUID userId) {
        String key = FAILURE_KEY + ":" + userId;
        notifications.findActiveByUserIdAndTypeAndMetadataValue(
                userId, NotificationType.SYSTEM, "refreshKey", key).ifPresent(notification -> {
                    notification.softDelete();
                    notifications.save(notification);
                });
    }
}
