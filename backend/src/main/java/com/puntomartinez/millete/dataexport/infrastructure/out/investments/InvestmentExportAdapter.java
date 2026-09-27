package com.puntomartinez.millete.dataexport.infrastructure.out.investments;

import com.puntomartinez.millete.dataexport.domain.model.InvestmentLedgerSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.InvestmentExportPort;
import com.puntomartinez.millete.investments.domain.model.*;
import com.puntomartinez.millete.investments.domain.ports.out.*;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Component
public class InvestmentExportAdapter implements InvestmentExportPort {
    private final AssetRepository assets;
    private final HoldingRepository holdings;
    private final ActivityRepository activities;
    private final UserCurrencyPort currencies;

    public InvestmentExportAdapter(AssetRepository assets, HoldingRepository holdings,
                                   ActivityRepository activities, UserCurrencyPort currencies) {
        this.assets = assets; this.holdings = holdings; this.activities = activities; this.currencies = currencies;
    }

    @Override public InvestmentLedgerSnapshot findAllByUserId(UUID userId) {
        List<InvestmentLedgerSnapshot.AssetSnapshot> assetSnapshots = assets.findAssetsByUserId(userId, true).stream()
                .map(a -> new InvestmentLedgerSnapshot.AssetSnapshot(a.getId(), a.getName(), a.getSymbol(), a.getType(),
                        a.getSectorId(), a.getCurrency(), a.getCreatedAt(), a.getModifiedAt(), a.isActive())).toList();
        List<InvestmentLedgerSnapshot.HoldingSnapshot> holdingSnapshots = holdings.findHoldingsIncludingSupersededByUserId(userId).stream()
                .map(h -> new InvestmentLedgerSnapshot.HoldingSnapshot(h.id(), h.assetId(), h.snapshotAt(), h.quantity(),
                        h.acquisitionCost(), h.currency(), h.historyIncomplete(), h.superseded(), h.createdAt())).toList();
        List<ActivityRepository.HistoricalActivity> history = activities.findActivityHistoryByUserId(userId);
        List<InvestmentLedgerSnapshot.ActivitySnapshot> activitySnapshots = history.stream()
                .map(h -> activitySnapshot(h.activity(), h.active())).toList();
        List<InvestmentLedgerSnapshot.ActivityAuditSnapshot> audits = history.stream()
                .flatMap(h -> activities.findAuditByActivityId(h.activity().getId(), userId).stream())
                .map(a -> new InvestmentLedgerSnapshot.ActivityAuditSnapshot(a.id(), a.activityId(), a.beforeJson(),
                        a.afterJson(), a.reason(), a.changedAt())).toList();
        List<InvestmentLedgerSnapshot.CurrencyPeriodSnapshot> currencyHistory = currencies.periods(userId).stream()
                .map(p -> new InvestmentLedgerSnapshot.CurrencyPeriodSnapshot(p.id(), p.currency(), p.validFrom(), p.validTo(), p.inferred())).toList();
        return new InvestmentLedgerSnapshot(assetSnapshots, holdingSnapshots, activitySnapshots, audits, currencyHistory);
    }

    private InvestmentLedgerSnapshot.ActivitySnapshot activitySnapshot(Activity a, boolean active) {
        return new InvestmentLedgerSnapshot.ActivitySnapshot(a.getId(), a.getType(), a.getOccurredAt(), a.getOrderingKey(),
                a.getAssetId(), a.getQuantity(), a.getUnitPrice(), a.getAmount(), a.getCurrency(), a.getSecondaryAmount(),
                a.getSecondaryCurrency(), a.getRatio(), a.getLocalCurrency(), a.getFxRateToLocal(), a.getFxRateSource(),
                a.getFxRateTimestamp(), a.getAmountInLocal(), a.getComment(), a.getLinkedTransactionId(),
                a.getCreatedAt(), a.getModifiedAt(), active);
    }
}
