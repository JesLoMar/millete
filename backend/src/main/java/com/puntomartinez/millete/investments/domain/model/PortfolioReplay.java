package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Pure deterministic replay of the investment ledger. */
public final class PortfolioReplay {
    public static final class LedgerIntegrityException extends IllegalArgumentException {
        private final String code;
        private final String resourceId;

        public LedgerIntegrityException(String code, String resourceId, String message) {
            super(message);
            this.code = code;
            this.resourceId = resourceId;
        }

        public String code() { return code; }
        public String resourceId() { return resourceId; }
    }

    public record Result(List<Lot> lots, List<LotConsumption> consumptions,
                         Map<String, BigDecimal> cashBalances) { }

    private PortfolioReplay() { }

    public static Result replay(UUID userId, List<Activity> activities,
                                List<Holding> holdings) {
        List<LedgerEvent> events = new ArrayList<>();
        activities.forEach(a -> events.add(new LedgerEvent(a.getOccurredAt(), a.getOrderingKey(), a.getId(), a, null)));
        holdings.forEach(h -> events.add(new LedgerEvent(h.snapshotAt(), 0, h.id(), null, h)));
        events.sort(Comparator.comparing(LedgerEvent::at)
                .thenComparingLong(LedgerEvent::order)
                .thenComparing(e -> e.id().toString()));

        Map<UUID, List<Lot>> lotsByAsset = new HashMap<>();
        List<LotConsumption> consumptions = new ArrayList<>();
        Map<String, BigDecimal> cash = new HashMap<>();

        long eventSequence = 0;
        for (LedgerEvent event : events) {
            long acquisitionOrder = eventSequence++;
            if (event.holding() != null) {
                Holding h = event.holding();
                UUID lotId = stableId("holding-lot", h.id());
                lotsByAsset.computeIfAbsent(h.assetId(), ignored -> new ArrayList<>())
                        .add(new Lot(lotId, userId, h.assetId(), null, h.id(), h.snapshotAt(),
                                acquisitionOrder, h.quantity(), h.quantity(), h.acquisitionCost(), h.currency(), true));
                continue;
            }
            Activity a = event.activity();
            switch (a.getType()) {
                case OPENING_CASH, DEPOSIT -> addCash(cash, a.getCurrency(), a.getAmount());
                case WITHDRAW -> subtractCash(cash, a.getCurrency(), a.getAmount(), a);
                case BUY -> {
                    subtractCash(cash, a.getCurrency(), a.getAmount(), a);
                    lotsByAsset.computeIfAbsent(a.getAssetId(), ignored -> new ArrayList<>())
                            .add(new Lot(stableId("buy-lot", a.getId()), userId, a.getAssetId(), a.getId(), null,
                                    a.getOccurredAt(), acquisitionOrder, a.getQuantity(), a.getQuantity(),
                                    a.getAmount(), a.getCurrency(), false));
                }
                case SELL -> {
                    addCash(cash, a.getCurrency(), a.getAmount());
                    consumeFifo(userId, a, lotsByAsset.getOrDefault(a.getAssetId(), List.of()), consumptions);
                }
                case DIVIDEND, INTEREST -> addCash(cash, a.getCurrency(), a.getAmount());
                case EXCHANGE -> {
                    subtractCash(cash, a.getCurrency(), a.getAmount(), a);
                    addCash(cash, a.getSecondaryCurrency(), a.getSecondaryAmount());
                }
                case SPLIT -> {
                    List<Lot> lots = lotsByAsset.get(a.getAssetId());
                    if (lots == null || lots.stream().noneMatch(l -> l.getRemainingQuantity().signum() > 0)) {
                        throw new IllegalArgumentException("SPLIT requires an open position");
                    }
                    lots.stream().filter(l -> l.getRemainingQuantity().signum() > 0).forEach(l -> l.split(a.getRatio()));
                }
            }
        }
        List<Lot> flatLots = lotsByAsset.values().stream().flatMap(List::stream).toList();
        return new Result(flatLots, List.copyOf(consumptions), Map.copyOf(cash));
    }

    private static void consumeFifo(UUID userId, Activity sell, List<Lot> lots,
                                    List<LotConsumption> consumptions) {
        BigDecimal remaining = sell.getQuantity();
        List<Lot> ordered = lots.stream()
                .filter(l -> l.getRemainingQuantity().signum() > 0)
                .sorted(Comparator.comparing(Lot::getAcquiredAt)
                        .thenComparingLong(Lot::getAcquisitionOrder)
                        .thenComparing(l -> l.getId().toString()))
                .toList();
        for (Lot lot : ordered) {
            if (remaining.signum() == 0) break;
            BigDecimal used = remaining.min(lot.getRemainingQuantity());
            BigDecimal cost = lot.consume(used);
            consumptions.add(new LotConsumption(stableId("consumption-" + sell.getId(), lot.getId()),
                    sell.getId(), lot.getId(), used, cost, lot.getCurrency()));
            remaining = remaining.subtract(used);
        }
        if (remaining.signum() > 0) {
            throw new LedgerIntegrityException("POSITION_NEGATIVE", sell.getAssetId().toString(),
                    "La venta " + sell.getId() + " excede las unidades disponibles del activo " + sell.getAssetId() + ".");
        }
    }

    private static void addCash(Map<String, BigDecimal> cash, String currency, BigDecimal amount) {
        cash.merge(currency, amount, BigDecimal::add);
    }

    private static void subtractCash(Map<String, BigDecimal> cash, String currency,
                                     BigDecimal amount, Activity activity) {
        BigDecimal next = cash.getOrDefault(currency, BigDecimal.ZERO).subtract(amount);
        if (next.signum() < 0) {
            throw new LedgerIntegrityException("CASH_NEGATIVE", currency,
                    "El saldo de efectivo " + currency + " sería negativo en la operación " + activity.getId() + ".");
        }
        cash.put(currency, next);
    }

    private static UUID stableId(String prefix, UUID sourceId) {
        return UUID.nameUUIDFromBytes((prefix + ":" + sourceId).getBytes(StandardCharsets.UTF_8));
    }

    private record LedgerEvent(java.time.Instant at, long order, UUID id,
                               Activity activity, Holding holding) { }
}
