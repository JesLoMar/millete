package com.puntomartinez.millete.dataexport.infrastructure.out.investments;

import com.puntomartinez.millete.dataexport.domain.model.InvestmentLedgerSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.InvestmentImportPort;
import com.puntomartinez.millete.investments.domain.model.*;
import com.puntomartinez.millete.investments.domain.ports.out.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class InvestmentImportAdapter implements InvestmentImportPort {
    private final AssetRepository assets;
    private final HoldingRepository holdings;
    private final ActivityRepository activities;
    private final PortfolioRepository portfolio;
    private final UserCurrencyPort currencies;
    private final DailyTransferPort dailyTransfers;

    public InvestmentImportAdapter(AssetRepository assets, HoldingRepository holdings,
                                   ActivityRepository activities, PortfolioRepository portfolio,
                                   UserCurrencyPort currencies, DailyTransferPort dailyTransfers) {
        this.assets = assets; this.holdings = holdings; this.activities = activities;
        this.portfolio = portfolio; this.currencies = currencies; this.dailyTransfers = dailyTransfers;
    }

    @Override @Transactional
    public int importInvestments(InvestmentLedgerSnapshot data, UUID userId) {
        if (data == null || data.legacyRecordsSkipped() > 0) {
            return 0;
        }
        List<InvestmentLedgerSnapshot.AssetSnapshot> assetRows = safe(data.assets());
        List<InvestmentLedgerSnapshot.HoldingSnapshot> holdingRows = safe(data.holdings());
        List<InvestmentLedgerSnapshot.ActivitySnapshot> activityRows = safe(data.activities());
        List<InvestmentLedgerSnapshot.ActivityAuditSnapshot> auditRows = safe(data.audit());
        List<InvestmentLedgerSnapshot.CurrencyPeriodSnapshot> currencyRows = safe(data.currencyHistory());

        Map<UUID, UUID> assetIds = new LinkedHashMap<>();
        for (var row : assetRows) {
            requireId(row.id(), "asset");
            UUID id = UUID.randomUUID();
            if (assetIds.putIfAbsent(row.id(), id) != null) throw new IllegalArgumentException("El ledger repite un ID de activo.");
            assets.save(Asset.reconstitute(id, userId, row.name(), row.symbol(), row.type(), row.sectorId(),
                    row.currency(), row.createdAt(), row.modifiedAt(), row.active()));
        }

        Map<UUID, UUID> holdingIds = new HashMap<>();
        for (var row : holdingRows) {
            requireId(row.id(), "holding");
            UUID assetId = requireMapped(assetIds, row.assetId(), "holding.assetId");
            UUID id = UUID.randomUUID();
            if (holdingIds.putIfAbsent(row.id(), id) != null) throw new IllegalArgumentException("El ledger repite un ID de Holding.");
            holdings.saveImported(new Holding(id, userId, assetId, row.snapshotAt(), row.quantity(),
                    row.acquisitionCost(), row.currency(), row.historyIncomplete(), row.superseded(), row.createdAt()));
        }

        if (!currencyRows.isEmpty()) {
            List<UserLocalCurrencyPeriod> periods = currencyRows.stream().map(row ->
                    new UserLocalCurrencyPeriod(UUID.randomUUID(), userId, row.currency(), row.validFrom(), row.validTo(), row.inferred())).toList();
            currencies.replacePeriods(userId, periods);
        }

        Map<UUID, UUID> activityIds = new LinkedHashMap<>();
        Map<UUID, UUID> generatedTransactionIds = new HashMap<>();
        List<InvestmentLedgerSnapshot.ActivitySnapshot> orderedActivities = activityRows.stream()
                .sorted(Comparator.comparing(InvestmentLedgerSnapshot.ActivitySnapshot::occurredAt)
                        .thenComparingLong(InvestmentLedgerSnapshot.ActivitySnapshot::orderingKey)
                        .thenComparing(row -> row.id().toString())).toList();
        for (var row : orderedActivities) {
            requireId(row.id(), "activity");
            UUID id = UUID.randomUUID();
            if (activityIds.putIfAbsent(row.id(), id) != null) throw new IllegalArgumentException("El ledger repite un ID de Activity.");
            UUID assetId = row.assetId() == null ? null : requireMapped(assetIds, row.assetId(), "activity.assetId");
            long nextOrderingKey = activities.nextOrderingKey(userId, row.occurredAt());
            Activity activity = Activity.reconstitute(id, userId, row.type(), row.occurredAt(), row.createdAt(),
                    row.modifiedAt(), nextOrderingKey, assetId, row.quantity(), row.unitPrice(), row.amount(),
                    row.currency(), row.secondaryAmount(), row.secondaryCurrency(), row.ratio(), row.localCurrency(),
                    row.fxRateToLocal(), row.fxRateSource(), row.fxRateTimestamp(), row.amountInLocal(), row.comment(), null);
            activities.saveImported(activity, row.active());
            if (row.linkedTransactionId() != null) {
                if (row.type() != ActivityType.DEPOSIT && row.type() != ActivityType.WITHDRAW) {
                    throw new IllegalArgumentException("Solo DEPOSIT/WITHDRAW puede vincular una transferencia diaria.");
                }
                DailyTransferPort.TransferDirection direction = row.type() == ActivityType.DEPOSIT
                        ? DailyTransferPort.TransferDirection.TRANSFER_OUT : DailyTransferPort.TransferDirection.TRANSFER_IN;
                UUID transactionId = dailyTransfers.createTransfer(userId, id, direction,
                        row.amountInLocal(), row.localCurrency(), row.occurredAt(), row.type().name() + " investment transfer");
                if (!dailyTransfers.existsTransfer(transactionId, userId)
                        || !dailyTransfers.matchesTransfer(userId, id, transactionId, direction,
                        row.amountInLocal(), row.localCurrency())) {
                    throw new IllegalArgumentException("No se pudo verificar la transferencia diaria vinculada a la Activity " + row.id());
                }
                if (generatedTransactionIds.put(row.linkedTransactionId(), transactionId) != null) {
                    throw new IllegalArgumentException("Una transferencia diaria está vinculada a más de una Activity.");
                }
            } else if (row.type() == ActivityType.DEPOSIT || row.type() == ActivityType.WITHDRAW) {
                throw new IllegalArgumentException("El historial contiene una Activity de efectivo sin transferencia diaria vinculada.");
            }
        }

        for (var row : auditRows) {
            UUID activityId = requireMapped(activityIds, row.activityId(), "audit.activityId");
            activities.saveAudit(new ActivityAudit(UUID.randomUUID(), activityId, userId,
                    row.beforeJson(), row.afterJson(), row.reason(), row.changedAt()));
        }

        // Rebuild lots and FIFO consumptions from the imported event ledger. Cash is replayed on read.
        List<Activity> allActivities = activities.findActivitiesByUserId(userId);
        List<Holding> currentHoldings = holdings.findHoldingsByUserId(userId);
        PortfolioReplay.Result replay = PortfolioReplay.replay(userId, allActivities, currentHoldings);
        for (Asset asset : assets.findAssetsByUserId(userId, true)) {
            List<Lot> lots = replay.lots().stream().filter(lot -> lot.getAssetId().equals(asset.getId())).toList();
            Set<UUID> lotIds = lots.stream().map(Lot::getId).collect(Collectors.toSet());
            List<LotConsumption> consumptions = replay.consumptions().stream().filter(c -> lotIds.contains(c.lotId())).toList();
            portfolio.replaceDerivedLots(userId, asset.getId(), lots, consumptions);
        }

        if (generatedTransactionIds.size() != activityRows.stream().filter(row -> row.linkedTransactionId() != null).count()) {
            throw new IllegalArgumentException("No se pudieron verificar todos los vínculos de transferencias de inversión.");
        }
        return assetRows.size() + holdingRows.size() + activityRows.size() + auditRows.size()
                + currencyRows.size() + generatedTransactionIds.size();
    }

    private static <T> List<T> safe(List<T> rows) { return rows == null ? List.of() : rows; }
    private static void requireId(UUID id, String name) { if (id == null) throw new IllegalArgumentException("Falta ID de " + name + " en el ledger."); }
    private static UUID requireMapped(Map<UUID, UUID> ids, UUID oldId, String relation) {
        UUID mapped = ids.get(oldId);
        if (mapped == null) throw new IllegalArgumentException("No se pudo resolver la relación " + relation + ": " + oldId);
        return mapped;
    }
}
