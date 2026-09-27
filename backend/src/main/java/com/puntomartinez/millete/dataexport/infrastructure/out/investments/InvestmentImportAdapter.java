package com.puntomartinez.millete.dataexport.infrastructure.out.investments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.puntomartinez.millete.dataexport.domain.model.InvestmentLedgerSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.InvestmentImportPort;
import com.puntomartinez.millete.investments.domain.model.*;
import com.puntomartinez.millete.investments.domain.ports.out.*;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;
import java.time.Instant;
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
    private final ObjectMapper mapper;
    private final TimeProvider time;

    public InvestmentImportAdapter(AssetRepository assets, HoldingRepository holdings,
                                   ActivityRepository activities, PortfolioRepository portfolio,
                                   UserCurrencyPort currencies, DailyTransferPort dailyTransfers,
                                   ObjectMapper mapper, TimeProvider time) {
        this.assets = assets; this.holdings = holdings; this.activities = activities;
        this.portfolio = portfolio; this.currencies = currencies; this.dailyTransfers = dailyTransfers;
        this.mapper = mapper; this.time = time;
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
        if (activityRows.stream().anyMatch(row -> row != null && !row.active())) {
            throw new IllegalArgumentException("El historial de inversiones no permite importar Activities inactivas.");
        }

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
            currencies.lockForUpdate(userId);
            List<UserLocalCurrencyPeriod> periods = currencyRows.stream().map(row ->
                    new UserLocalCurrencyPeriod(UUID.randomUUID(), userId, row.currency(), row.validFrom(), row.validTo(), row.inferred())).toList();
            validateCurrencyHistory(userId, periods, currencies.currentCurrency(userId), time.now());
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
            activities.save(activity);
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
                    remapAuditJson(row.beforeJson(), activityIds, assetIds, generatedTransactionIds, userId),
                    remapAuditJson(row.afterJson(), activityIds, assetIds, generatedTransactionIds, userId),
                    row.reason(), row.changedAt()));
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

    private static void validateCurrencyHistory(UUID userId, List<UserLocalCurrencyPeriod> periods,
                                                Optional<String> currentCurrency, Instant now) {
        List<UserLocalCurrencyPeriod> ordered = periods.stream()
                .sorted(Comparator.comparing(UserLocalCurrencyPeriod::validFrom)).toList();
        UserLocalCurrencyPeriod previous = null;
        UserLocalCurrencyPeriod open = null;
        for (UserLocalCurrencyPeriod period : ordered) {
            if (!period.userId().equals(userId)) {
                throw new IllegalArgumentException("El historial de moneda local pertenece a otro usuario.");
            }
            if (previous != null && (previous.validTo() == null || previous.validTo().isAfter(period.validFrom()))) {
                throw new IllegalArgumentException("Los periodos de moneda local importados se solapan.");
            }
            if (period.validTo() == null) {
                if (open != null) throw new IllegalArgumentException("El historial tiene más de un periodo de moneda abierto.");
                open = period;
            }
            previous = period;
        }

        if (currentCurrency.isPresent()) {
            if (open == null || !open.currency().equalsIgnoreCase(currentCurrency.get())
                    || open.validFrom().isAfter(now)) {
                throw new IllegalArgumentException("La moneda de las preferencias debe coincidir con el periodo de moneda local abierto y vigente.");
            }
        } else if (open != null) {
            throw new IllegalArgumentException("No puede haber un periodo de moneda local abierto si las preferencias no definen localCurrency.");
        }
    }

    private String remapAuditJson(String json, Map<UUID, UUID> activityIds,
                                 Map<UUID, UUID> assetIds, Map<UUID, UUID> transactionIds,
                                 UUID userId) {
        try {
            JsonNode tree = mapper.readTree(json);
            if (!(tree instanceof ObjectNode snapshot)) {
                throw new IllegalArgumentException("Una auditoría de Activity debe contener un objeto JSON.");
            }
            remapUuid(snapshot, "id", activityIds, "audit.activity.id");
            remapUserId(snapshot, userId);
            remapUuid(snapshot, "assetId", assetIds, "audit.activity.assetId");
            remapUuid(snapshot, "linkedTransactionId", transactionIds, "audit.activity.linkedTransactionId");
            return mapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("No se pudo leer una instantánea JSON de auditoría.", exception);
        }
    }

    private static void remapUuid(ObjectNode snapshot, String field, Map<UUID, UUID> ids,
                                  String relation) {
        JsonNode value = snapshot.get(field);
        if (value == null || value.isNull()) return;
        if (!value.isTextual()) {
            throw new IllegalArgumentException("La identidad " + relation + " de la auditoría no es un UUID válido.");
        }
        UUID oldId;
        try {
            oldId = UUID.fromString(value.textValue());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("La identidad " + relation + " de la auditoría no es un UUID válido.", exception);
        }
        snapshot.put(field, requireMapped(ids, oldId, relation).toString());
    }

    private static void remapUserId(ObjectNode snapshot, UUID userId) {
        JsonNode value = snapshot.get("userId");
        if (value == null || value.isNull()) return;
        if (!value.isTextual()) {
            throw new IllegalArgumentException("La identidad audit.activity.userId de la auditoría no es un UUID válido.");
        }
        try {
            UUID.fromString(value.textValue());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("La identidad audit.activity.userId de la auditoría no es un UUID válido.", exception);
        }
        snapshot.put("userId", userId.toString());
    }

    private static UUID requireMapped(Map<UUID, UUID> ids, UUID oldId, String relation) {
        UUID mapped = ids.get(oldId);
        if (mapped == null) throw new IllegalArgumentException("No se pudo resolver la relación " + relation + ": " + oldId);
        return mapped;
    }
}
