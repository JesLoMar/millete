package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PortfolioReplay {

    public static final class LedgerIntegrityException
            extends IllegalArgumentException {

        private final String code;
        private final String resourceId;

        public LedgerIntegrityException(
                String code,
                String resourceId,
                String message
        ) {
            super(message);
            this.code = code;
            this.resourceId = resourceId;
        }

        public String getCode() {
            return code;
        }

        public String getResourceId() {
            return resourceId;
        }
    }

    public record Result(
            List<Lot> lots,
            List<LotConsumption> consumptions,
            Map<CurrencyCode, Money> cashBalances,
            List<Position> positions
    ) {
    }

    private PortfolioReplay() {
    }

    public static Result replay(
            UUID userId,
            List<Activity> activities,
            List<Holding> holdings,
            Instant trackingStartAt
    ) {
        validateInput(
                userId,
                activities,
                holdings,
                trackingStartAt
        );

        List<Holding> activeHoldings =
                holdings.stream()
                        .filter(
                                holding ->
                                        holding.getStatus()
                                                == HoldingStatus.ACTIVE
                        )
                        .toList();

        validateOwnership(
                userId,
                activities,
                activeHoldings
        );

        validateActiveHoldingUniqueness(
                activeHoldings
        );

        validateHoldingBoundaries(
                activities,
                activeHoldings
        );

        Map<AssetReference, List<Lot>> lotsByAsset =
                new HashMap<>();

        List<LotConsumption> consumptions =
                new ArrayList<>();

        Map<CurrencyCode, Money> cashBalances =
                new LinkedHashMap<>();

        long acquisitionSequence = 0;

        /*
         * Los Holdings ACTIVE representan el estado inicial
         * del historial incompleto.
         */
        for (Holding holding : activeHoldings) {

            Lot lot = Lot.fromHolding(
                    stableId(
                            "holding-lot",
                            holding.getId()
                    ),
                    userId,
                    holding.getAssetReference(),
                    holding.getId(),
                    holding.getSnapshotAt(),
                    acquisitionSequence++,
                    holding.getQuantity(),
                    holding.getAcquisitionCost()
            );

            lotsByAsset
                    .computeIfAbsent(
                            holding.getAssetReference(),
                            ignored -> new ArrayList<>()
                    )
                    .add(lot);
        }

        List<Activity> orderedActivities =
                activities.stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                Activity::getOccurredAt
                                        )
                                        .thenComparingLong(
                                                Activity::getOrderingKey
                                        )
                                        .thenComparing(
                                                activity ->
                                                        activity
                                                                .getId()
                                                                .toString()
                                        )
                        )
                        .toList();

        for (Activity activity : orderedActivities) {

            boolean cashTracked =
                    !activity.getOccurredAt()
                            .isBefore(trackingStartAt);

            long acquisitionOrder =
                    acquisitionSequence++;

            processActivity(
                    userId,
                    activity,
                    cashTracked,
                    acquisitionOrder,
                    lotsByAsset,
                    cashBalances,
                    consumptions
            );
        }

        List<Lot> lots =
                lotsByAsset.values()
                        .stream()
                        .flatMap(List::stream)
                        .toList();

        List<Position> positions =
                buildPositions(
                        userId,
                        lotsByAsset
                );

        return new Result(
                lots,
                consumptions,
                Map.copyOf(cashBalances),
                positions
        );
    }

    private static void processActivity(
            UUID userId,
            Activity activity,
            boolean cashTracked,
            long acquisitionOrder,
            Map<AssetReference, List<Lot>> lotsByAsset,
            Map<CurrencyCode, Money> cashBalances,
            List<LotConsumption> consumptions
    ) {
        switch (activity.getType()) {

            case OPENING_POSITION -> {
                OpeningPositionDetails details =
                        (OpeningPositionDetails)
                                activity.getDetails();

                addLot(
                        lotsByAsset,
                        Lot.fromActivity(
                                stableId(
                                        "opening-position-lot",
                                        activity.getId()
                                ),
                                userId,
                                activity.getAssetReference(),
                                activity.getId(),
                                activity.getOccurredAt(),
                                acquisitionOrder,
                                details.quantity(),
                                details.acquisitionCost()
                        )
                );
            }

            case BUY -> {
                TradeActivityDetails details =
                        (TradeActivityDetails)
                                activity.getDetails();

                if (cashTracked) {
                    subtractCash(
                            cashBalances,
                            details.settlement().amount(),
                            activity
                    );
                }

                /*
                 * El coste del Lot se expresa siempre en la moneda
                 * propia del Asset.
                 */
                addLot(
                        lotsByAsset,
                        Lot.fromActivity(
                                stableId(
                                        "buy-lot",
                                        activity.getId()
                                ),
                                userId,
                                activity.getAssetReference(),
                                activity.getId(),
                                activity.getOccurredAt(),
                                acquisitionOrder,
                                details.quantity(),
                                details.amount()
                        )
                );
            }

            case SELL -> {
                TradeActivityDetails details =
                        (TradeActivityDetails)
                                activity.getDetails();

                consumeFifo(
                        activity,
                        details.quantity(),
                        lotsByAsset.getOrDefault(
                                activity.getAssetReference(),
                                List.of()
                        ),
                        consumptions
                );

                if (cashTracked) {
                    addCash(
                            cashBalances,
                            details.settlement().amount()
                    );
                }
            }

            case DIVIDEND -> processDividend(
                    userId,
                    activity,
                    cashTracked,
                    acquisitionOrder,
                    lotsByAsset,
                    cashBalances
            );

            case DEPOSIT -> {
                if (cashTracked) {
                    CashActivityDetails details =
                            (CashActivityDetails)
                                    activity.getDetails();

                    addCash(
                            cashBalances,
                            details.amount()
                    );
                }
            }

            case WITHDRAW -> {
                if (cashTracked) {
                    CashActivityDetails details =
                            (CashActivityDetails)
                                    activity.getDetails();

                    subtractCash(
                            cashBalances,
                            details.amount(),
                            activity
                    );
                }
            }

            case OPENING_CASH -> {
                if (cashTracked) {
                    CashActivityDetails details =
                            (CashActivityDetails)
                                    activity.getDetails();

                    addCash(
                            cashBalances,
                            details.amount()
                    );
                }
            }

            case EXCHANGE -> {
                if (cashTracked) {
                    ExchangeActivityDetails details =
                            (ExchangeActivityDetails)
                                    activity.getDetails();

                    subtractCash(
                            cashBalances,
                            details.origin(),
                            activity
                    );

                    addCash(
                            cashBalances,
                            details.destination()
                    );
                }
            }

            case SPLIT -> {
                SplitActivityDetails details =
                        (SplitActivityDetails)
                                activity.getDetails();

                List<Lot> lots =
                        lotsByAsset.getOrDefault(
                                activity.getAssetReference(),
                                List.of()
                        );

                if (lots.stream()
                        .noneMatch(
                                lot ->
                                        lot.getRemainingQuantity()
                                                .signum() > 0
                        )) {

                    throw new LedgerIntegrityException(
                            "POSITION_REQUIRED",
                            activity.getAssetReference()
                                    .id()
                                    .toString(),
                            "Un SPLIT requiere una posición abierta"
                    );
                }

                lots.stream()
                        .filter(
                                lot ->
                                        lot.getRemainingQuantity()
                                                .signum() > 0
                        )
                        .forEach(
                                lot ->
                                        lot.split(
                                                details.ratio()
                                        )
                        );
            }
        }
    }

    private static void processDividend(
            UUID userId,
            Activity activity,
            boolean cashTracked,
            long acquisitionOrder,
            Map<AssetReference, List<Lot>> lotsByAsset,
            Map<CurrencyCode, Money> cashBalances
    ) {
        if (activity.getDetails()
                instanceof CashActivityDetails details) {

            if (cashTracked) {
                addCash(
                        cashBalances,
                        details.amount()
                );
            }

            return;
        }

        DividendUnitsDetails details =
                (DividendUnitsDetails)
                        activity.getDetails();

        Money zeroCost =
                Money.zero(
                        details.referenceUnitPrice()
                                .currency()
                );

        addLot(
                lotsByAsset,
                Lot.fromActivity(
                        stableId(
                                "dividend-in-kind-lot",
                                activity.getId()
                        ),
                        userId,
                        activity.getAssetReference(),
                        activity.getId(),
                        activity.getOccurredAt(),
                        acquisitionOrder,
                        details.quantity(),
                        zeroCost
                )
        );
    }

    private static void addLot(
            Map<AssetReference, List<Lot>> lotsByAsset,
            Lot lot
    ) {
        lotsByAsset
                .computeIfAbsent(
                        lot.getAssetReference(),
                        ignored -> new ArrayList<>()
                )
                .add(lot);
    }

    private static void consumeFifo(
            Activity sell,
            BigDecimal quantity,
            List<Lot> lots,
            List<LotConsumption> consumptions
    ) {
        BigDecimal remaining = quantity;

        List<Lot> orderedLots =
                lots.stream()
                        .filter(
                                lot ->
                                        lot.getRemainingQuantity()
                                                .signum() > 0
                        )
                        .sorted(
                                Comparator
                                        .comparing(
                                                Lot::getAcquiredAt
                                        )
                                        .thenComparingLong(
                                                Lot::getAcquisitionOrder
                                        )
                                        .thenComparing(
                                                lot ->
                                                        lot.getId()
                                                                .toString()
                                        )
                        )
                        .toList();

        for (Lot lot : orderedLots) {

            if (remaining.signum() == 0) {
                break;
            }

            BigDecimal consumed =
                    remaining.min(
                            lot.getRemainingQuantity()
                    );

            Money costBasis =
                    lot.consume(consumed);

            consumptions.add(
                    new LotConsumption(
                            stableConsumptionId(
                                    sell.getId(),
                                    lot.getId()
                            ),
                            sell.getId(),
                            lot.getId(),
                            consumed,
                            costBasis
                    )
            );

            remaining =
                    remaining.subtract(consumed);
        }

        if (remaining.signum() > 0) {
            throw new LedgerIntegrityException(
                    "POSITION_NEGATIVE",
                    sell.getAssetReference()
                            .id()
                            .toString(),
                    "La venta excede las unidades disponibles"
            );
        }
    }

    private static List<Position> buildPositions(
            UUID userId,
            Map<AssetReference, List<Lot>> lotsByAsset
    ) {
        List<Position> positions =
                new ArrayList<>();

        for (Map.Entry<AssetReference, List<Lot>> entry
                : lotsByAsset.entrySet()) {

            List<Lot> openLots =
                    entry.getValue()
                            .stream()
                            .filter(
                                    lot ->
                                            lot.getRemainingQuantity()
                                                    .signum() > 0
                            )
                            .toList();

            if (openLots.isEmpty()) {
                continue;
            }

            Money acquisitionCost =
                    Money.zero(
                            openLots.get(0)
                                    .getTotalCost()
                                    .currency()
                    );

            boolean historyIncomplete = false;

            BigDecimal quantity =
                    BigDecimal.ZERO;

            for (Lot lot : openLots) {

                if (!acquisitionCost.currency()
                        .equals(
                                lot.getTotalCost()
                                        .currency()
                        )) {

                    throw new LedgerIntegrityException(
                            "INCONSISTENT_CURRENCY",
                            entry.getKey()
                                    .id()
                                    .toString(),
                            "Los Lots del mismo Asset tienen monedas distintas"
                    );
                }

                quantity =
                        quantity.add(
                                lot.getRemainingQuantity()
                        );

                acquisitionCost =
                        acquisitionCost.add(
                                lot.getUnitCost()
                                        .multiply(
                                                lot.getRemainingQuantity(),
                                                Lot.COST_BASIS_SCALE
                                        )
                        );

                historyIncomplete |=
                        lot.isSynthetic();
            }

            positions.add(
                    new Position(
                            userId,
                            entry.getKey(),
                            quantity,
                            acquisitionCost,
                            historyIncomplete,
                            historyIncomplete
                    )
            );
        }

        return List.copyOf(positions);
    }

    private static void addCash(
            Map<CurrencyCode, Money> cashBalances,
            Money money
    ) {
        cashBalances.merge(
                money.currency(),
                money,
                Money::add
        );
    }

    private static void subtractCash(
            Map<CurrencyCode, Money> cashBalances,
            Money money,
            Activity activity
    ) {
        Money current =
                cashBalances.getOrDefault(
                        money.currency(),
                        Money.zero(
                                money.currency()
                        )
                );

        Money next =
                current.subtract(money);

        if (next.amount().signum() < 0) {
            throw new LedgerIntegrityException(
                    "CASH_NEGATIVE",
                    money.currency().value(),
                    "El saldo de "
                            + money.currency().value()
                            + " sería negativo en la Activity "
                            + activity.getId()
            );
        }

        cashBalances.put(
                money.currency(),
                next
        );
    }

    private static void validateInput(
            UUID userId,
            List<Activity> activities,
            List<Holding> holdings,
            Instant trackingStartAt
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

        if (activities == null) {
            throw new IllegalArgumentException(
                    "Las Activities son obligatorias"
            );
        }

        if (holdings == null) {
            throw new IllegalArgumentException(
                    "Los Holdings son obligatorios"
            );
        }

        if (trackingStartAt == null) {
            throw new IllegalArgumentException(
                    "trackingStartAt es obligatorio"
            );
        }
    }

    private static void validateOwnership(
            UUID userId,
            List<Activity> activities,
            List<Holding> holdings
    ) {
        for (Activity activity : activities) {
            if (!userId.equals(activity.getUserId())) {
                throw new LedgerIntegrityException(
                        "USER_MISMATCH",
                        activity.getId().toString(),
                        "La Activity no pertenece al usuario del replay"
                );
            }
        }

        for (Holding holding : holdings) {
            if (!userId.equals(holding.getUserId())) {
                throw new LedgerIntegrityException(
                        "USER_MISMATCH",
                        holding.getId().toString(),
                        "El Holding no pertenece al usuario del replay"
                );
            }
        }
    }

    private static void validateActiveHoldingUniqueness(
            List<Holding> activeHoldings
    ) {
        Map<AssetReference, UUID> seen =
                new HashMap<>();

        for (Holding holding : activeHoldings) {

            UUID existing =
                    seen.putIfAbsent(
                            holding.getAssetReference(),
                            holding.getId()
                    );

            if (existing != null) {
                throw new LedgerIntegrityException(
                        "MULTIPLE_ACTIVE_HOLDINGS",
                        holding.getAssetReference()
                                .id()
                                .toString(),
                        "Solo puede existir un Holding ACTIVE por Asset"
                );
            }
        }
    }

    private static void validateHoldingBoundaries(
            List<Activity> activities,
            List<Holding> activeHoldings
    ) {
        for (Holding holding : activeHoldings) {

            for (Activity activity : activities) {

                if (!holding.getAssetReference()
                        .equals(
                                activity.getAssetReference()
                        )) {

                    continue;
                }

                if (!activity.getOccurredAt()
                        .isAfter(
                                holding.getSnapshotAt()
                        )) {

                    throw new LedgerIntegrityException(
                            "HOLDING_ACTIVITY_CONFLICT",
                            holding.getAssetReference()
                                    .id()
                                    .toString(),
                            "Un Holding ACTIVE no puede coexistir "
                                    + "con una Activity anterior o igual a snapshotAt"
                    );
                }
            }
        }
    }

    private static UUID stableId(
            String prefix,
            UUID sourceId
    ) {
        return UUID.nameUUIDFromBytes(
                (prefix + ":" + sourceId)
                        .getBytes(
                                StandardCharsets.UTF_8
                        )
        );
    }

    private static UUID stableConsumptionId(
            UUID sellActivityId,
            UUID lotId
    ) {
        return UUID.nameUUIDFromBytes(
                (
                        "consumption:"
                                + sellActivityId
                                + ":"
                                + lotId
                ).getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }
}