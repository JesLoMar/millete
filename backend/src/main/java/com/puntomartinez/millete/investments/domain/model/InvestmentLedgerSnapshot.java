package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Snapshot portable del ledger de Investments.
 *
 * Contiene únicamente datos fuente necesarios para reconstruir
 * el bounded context. No contiene estado derivado.
 *
 * No incluye:
 * - Lots
 * - LotConsumptions
 * - Positions
 * - Investment Cash derivado
 * - SharedAssets completos
 * - Transactions derivadas
 * - historial de moneda local
 * - orderingKey técnico
 */
public record InvestmentLedgerSnapshot(
        int version,
        Instant trackingStartAt,
        List<UserAssetSnapshot> userAssets,
        List<UserAssetPriceSnapshot> userAssetPrices,
        List<HoldingSnapshot> holdings,
        List<ActivitySnapshot> activities,
        List<ActivityAuditSnapshot> audits
) {

    public static final int CURRENT_VERSION = 1;

    public InvestmentLedgerSnapshot {
        if (version <= 0) {
            throw new IllegalArgumentException(
                    "La versión del snapshot debe ser positiva"
            );
        }

        if (trackingStartAt == null) {
            throw new IllegalArgumentException(
                    "trackingStartAt es obligatorio"
            );
        }

        userAssets = copyRequiredList(
                userAssets,
                "userAssets"
        );

        userAssetPrices = copyRequiredList(
                userAssetPrices,
                "userAssetPrices"
        );

        holdings = copyRequiredList(
                holdings,
                "holdings"
        );

        activities = copyRequiredList(
                activities,
                "activities"
        );

        audits = copyRequiredList(
                audits,
                "audits"
        );
    }

    private static <T> List<T> copyRequiredList(
            List<T> values,
            String name
    ) {
        if (values == null) {
            throw new IllegalArgumentException(
                    name + " es obligatorio"
            );
        }

        return List.copyOf(values);
    }

    /**
     * Referencia portable a un Asset.
     *
     * SHARED:
     * - no se utiliza el UUID interno de la instalación origen;
     * - se utiliza stableCatalogId.
     *
     * USER:
     * - se utiliza el UUID del UserAsset del snapshot origen;
     * - Restore lo remapea al UUID de destino.
     */
    public record AssetReferenceSnapshot(
            AssetReferenceKind kind,
            UUID userAssetId,
            String sharedAssetStableCatalogId
    ) {

        public AssetReferenceSnapshot {
            if (kind == null) {
                throw new IllegalArgumentException(
                        "El tipo de referencia del Asset es obligatorio"
                );
            }

            if (kind == AssetReferenceKind.USER) {
                if (userAssetId == null) {
                    throw new IllegalArgumentException(
                            "Una referencia USER requiere userAssetId"
                    );
                }

                if (sharedAssetStableCatalogId != null) {
                    throw new IllegalArgumentException(
                            "Una referencia USER no puede contener "
                                    + "sharedAssetStableCatalogId"
                    );
                }
            }

            if (kind == AssetReferenceKind.SHARED) {
                if (sharedAssetStableCatalogId == null
                        || sharedAssetStableCatalogId.isBlank()) {

                    throw new IllegalArgumentException(
                            "Una referencia SHARED requiere "
                                    + "sharedAssetStableCatalogId"
                    );
                }

                if (userAssetId != null) {
                    throw new IllegalArgumentException(
                            "Una referencia SHARED no puede contener "
                                    + "userAssetId"
                    );
                }

                sharedAssetStableCatalogId =
                        sharedAssetStableCatalogId.trim();
            }
        }

        public static AssetReferenceSnapshot user(
                UUID userAssetId
        ) {
            return new AssetReferenceSnapshot(
                    AssetReferenceKind.USER,
                    userAssetId,
                    null
            );
        }

        public static AssetReferenceSnapshot shared(
                String stableCatalogId
        ) {
            return new AssetReferenceSnapshot(
                    AssetReferenceKind.SHARED,
                    null,
                    stableCatalogId
            );
        }
    }

    /**
     * Representación portable de Money.
     *
     * Se utilizan tipos simples para que el snapshot no dependa
     * de cómo se serialicen internamente CurrencyCode o Money.
     */
    public record MoneySnapshot(
            BigDecimal amount,
            String currency
    ) {

        public MoneySnapshot {
            if (amount == null) {
                throw new IllegalArgumentException(
                        "El importe es obligatorio"
                );
            }

            if (amount.signum() < 0) {
                throw new IllegalArgumentException(
                        "El importe no puede ser negativo"
                );
            }

            if (currency == null || currency.isBlank()) {
                throw new IllegalArgumentException(
                        "La moneda es obligatoria"
                );
            }

            CurrencyCode.of(currency);

            currency = currency.trim().toUpperCase();
        }
    }

    /**
     * Snapshot del FX realmente utilizado por una Activity.
     */
    public record AppliedFxRateSnapshot(
            String baseCurrency,
            String quoteCurrency,
            BigDecimal rate,
            String source,
            Instant timestamp
    ) {

        public AppliedFxRateSnapshot {
            if (baseCurrency == null
                    || baseCurrency.isBlank()) {

                throw new IllegalArgumentException(
                        "La moneda base es obligatoria"
                );
            }

            if (quoteCurrency == null
                    || quoteCurrency.isBlank()) {

                throw new IllegalArgumentException(
                        "La moneda quote es obligatoria"
                );
            }

            CurrencyCode base =
                    CurrencyCode.of(baseCurrency);

            CurrencyCode quote =
                    CurrencyCode.of(quoteCurrency);

            if (base.equals(quote)) {
                throw new IllegalArgumentException(
                        "Las monedas del FX deben ser distintas"
                );
            }

            if (rate == null || rate.signum() <= 0) {
                throw new IllegalArgumentException(
                        "El tipo de cambio debe ser positivo"
                );
            }

            if (source == null || source.isBlank()) {
                throw new IllegalArgumentException(
                        "La fuente del FX es obligatoria"
                );
            }

            if (timestamp == null) {
                throw new IllegalArgumentException(
                        "La fecha del FX es obligatoria"
                );
            }

            baseCurrency =
                    base.value();

            quoteCurrency =
                    quote.value();

            source = source.trim();
        }
    }

    public record AssetSectorSnapshot(
            String code,
            String displayName,
            boolean custom
    ) {

        public AssetSectorSnapshot {
            if (displayName == null
                    || displayName.isBlank()) {

                throw new IllegalArgumentException(
                        "El nombre del sector es obligatorio"
                );
            }

            displayName = displayName.trim();

            if (custom) {
                if (code != null && !code.isBlank()) {
                    throw new IllegalArgumentException(
                            "Un sector personalizado no puede tener code"
                    );
                }

                code = null;
            } else {
                if (code == null || code.isBlank()) {
                    throw new IllegalArgumentException(
                            "Un sector común requiere code"
                    );
                }

                code = code.trim().toUpperCase();
            }
        }
    }

    public record UserAssetSnapshot(
            UUID id,
            String name,
            AssetType type,
            AssetSectorSnapshot sector,
            String origin,
            String currency,
            Instant createdAt,
            Instant modifiedAt
    ) {

        public UserAssetSnapshot {
            if (id == null) {
                throw new IllegalArgumentException(
                        "El identificador del UserAsset es obligatorio"
                );
            }

            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException(
                        "El nombre del UserAsset es obligatorio"
                );
            }

            if (type == null) {
                throw new IllegalArgumentException(
                        "El tipo del UserAsset es obligatorio"
                );
            }

            if (sector == null) {
                throw new IllegalArgumentException(
                        "El sector del UserAsset es obligatorio"
                );
            }

            if (origin == null || origin.isBlank()) {
                throw new IllegalArgumentException(
                        "El origen del UserAsset es obligatorio"
                );
            }

            if (currency == null || currency.isBlank()) {
                throw new IllegalArgumentException(
                        "La moneda del UserAsset es obligatoria"
                );
            }

            CurrencyCode.of(currency);

            if (createdAt == null
                    || modifiedAt == null) {

                throw new IllegalArgumentException(
                        "Las fechas del UserAsset son obligatorias"
                );
            }

            name = name.trim();
            origin = origin.trim().toUpperCase();
            currency = currency.trim().toUpperCase();
        }
    }

    public record UserAssetPriceSnapshot(
            UUID id,
            UUID userAssetId,
            MoneySnapshot unitPrice,
            Instant timestamp
    ) {

        public UserAssetPriceSnapshot {
            if (id == null) {
                throw new IllegalArgumentException(
                        "El identificador del UserAssetPrice es obligatorio"
                );
            }

            if (userAssetId == null) {
                throw new IllegalArgumentException(
                        "El UserAsset es obligatorio"
                );
            }

            if (unitPrice == null) {
                throw new IllegalArgumentException(
                        "El precio unitario es obligatorio"
                );
            }

            if (unitPrice.amount().signum() < 0) {
                throw new IllegalArgumentException(
                        "El precio unitario no puede ser negativo"
                );
            }

            if (timestamp == null) {
                throw new IllegalArgumentException(
                        "La fecha del UserAssetPrice es obligatoria"
                );
            }
        }
    }

    public record HoldingSnapshot(
            UUID id,
            AssetReferenceSnapshot assetReference,
            Instant snapshotAt,
            BigDecimal quantity,
            MoneySnapshot acquisitionCost,
            HoldingStatus status,
            Instant createdAt,
            Instant supersededAt
    ) {

        public HoldingSnapshot {
            if (id == null) {
                throw new IllegalArgumentException(
                        "El identificador del Holding es obligatorio"
                );
            }

            if (assetReference == null) {
                throw new IllegalArgumentException(
                        "El AssetReference del Holding es obligatorio"
                );
            }

            if (snapshotAt == null) {
                throw new IllegalArgumentException(
                        "snapshotAt es obligatorio"
                );
            }

            if (quantity == null
                    || quantity.signum() <= 0) {

                throw new IllegalArgumentException(
                        "La cantidad del Holding debe ser positiva"
                );
            }

            if (acquisitionCost == null) {
                throw new IllegalArgumentException(
                        "El coste de adquisición es obligatorio"
                );
            }

            if (status == null) {
                throw new IllegalArgumentException(
                        "El estado del Holding es obligatorio"
                );
            }

            if (createdAt == null) {
                throw new IllegalArgumentException(
                        "createdAt es obligatorio"
                );
            }

            if (status == HoldingStatus.ACTIVE
                    && supersededAt != null) {

                throw new IllegalArgumentException(
                        "Un Holding ACTIVE no puede tener supersededAt"
                );
            }

            if (status == HoldingStatus.SUPERSEDED
                    && supersededAt == null) {

                throw new IllegalArgumentException(
                        "Un Holding SUPERSEDED requiere supersededAt"
                );
            }
        }
    }

    public sealed interface ActivityDetailsSnapshot
            permits TradeDetailsSnapshot,
            CashDetailsSnapshot,
            DividendUnitsDetailsSnapshot,
            ExchangeDetailsSnapshot,
            SplitDetailsSnapshot,
            OpeningPositionDetailsSnapshot {
    }

    public record TradeDetailsSnapshot(
            BigDecimal quantity,
            MoneySnapshot unitPrice,
            MoneySnapshot settlementAmount,
            AppliedFxRateSnapshot appliedFxRate
    ) implements ActivityDetailsSnapshot {

        public TradeDetailsSnapshot {
            if (quantity == null
                    || quantity.signum() <= 0) {

                throw new IllegalArgumentException(
                        "La cantidad del Trade debe ser positiva"
                );
            }

            if (unitPrice == null
                    || unitPrice.amount().signum() <= 0) {

                throw new IllegalArgumentException(
                        "El precio unitario del Trade debe ser positivo"
                );
            }

            if (settlementAmount == null
                    || settlementAmount.amount().signum() <= 0) {

                throw new IllegalArgumentException(
                        "El settlementAmount debe ser positivo"
                );
            }
        }
    }

    public record CashDetailsSnapshot(
            MoneySnapshot amount
    ) implements ActivityDetailsSnapshot {

        public CashDetailsSnapshot {
            if (amount == null
                    || amount.amount().signum() <= 0) {

                throw new IllegalArgumentException(
                        "El importe de Cash debe ser positivo"
                );
            }
        }
    }

    public record DividendUnitsDetailsSnapshot(
            BigDecimal quantity,
            MoneySnapshot referenceUnitPrice
    ) implements ActivityDetailsSnapshot {

        public DividendUnitsDetailsSnapshot {
            if (quantity == null
                    || quantity.signum() <= 0) {

                throw new IllegalArgumentException(
                        "La cantidad del dividendo en unidades "
                                + "debe ser positiva"
                );
            }

            if (referenceUnitPrice == null
                    || referenceUnitPrice.amount().signum() <= 0) {

                throw new IllegalArgumentException(
                        "El referenceUnitPrice debe ser positivo"
                );
            }
        }
    }

    public record ExchangeDetailsSnapshot(
            MoneySnapshot origin,
            MoneySnapshot destination,
            AppliedFxRateSnapshot appliedFxRate
    ) implements ActivityDetailsSnapshot {

        public ExchangeDetailsSnapshot {
            if (origin == null
                    || origin.amount().signum() <= 0) {

                throw new IllegalArgumentException(
                        "El importe origen debe ser positivo"
                );
            }

            if (destination == null
                    || destination.amount().signum() <= 0) {

                throw new IllegalArgumentException(
                        "El importe destino debe ser positivo"
                );
            }

            if (origin.currency()
                    .equals(destination.currency())) {

                throw new IllegalArgumentException(
                        "Las monedas de EXCHANGE deben ser distintas"
                );
            }

            if (appliedFxRate == null) {
                throw new IllegalArgumentException(
                        "EXCHANGE requiere appliedFxRate"
                );
            }
        }
    }

    public record SplitDetailsSnapshot(
            BigDecimal ratio
    ) implements ActivityDetailsSnapshot {

        public SplitDetailsSnapshot {
            if (ratio == null
                    || ratio.signum() <= 0) {

                throw new IllegalArgumentException(
                        "El ratio del SPLIT debe ser positivo"
                );
            }
        }
    }

    public record OpeningPositionDetailsSnapshot(
            BigDecimal quantity,
            MoneySnapshot acquisitionCost
    ) implements ActivityDetailsSnapshot {

        public OpeningPositionDetailsSnapshot {
            if (quantity == null
                    || quantity.signum() <= 0) {

                throw new IllegalArgumentException(
                        "La cantidad de OPENING_POSITION "
                                + "debe ser positiva"
                );
            }

            if (acquisitionCost == null) {
                throw new IllegalArgumentException(
                        "El coste de OPENING_POSITION es obligatorio"
                );
            }
        }
    }

    /**
     * Snapshot portable de una Activity.
     *
     * orderingKey NO se exporta.
     *
     * sequence conserva únicamente el orden relativo necesario
     * para que Restore pueda generar nuevos orderingKey locales.
     *
     * linkedTransactionId tampoco se exporta:
     * las Transactions de DEPOSIT/WITHDRAW se regeneran durante Restore.
     */
    public record ActivitySnapshot(
            UUID id,
            ActivityType type,
            AssetReferenceSnapshot assetReference,
            Instant occurredAt,
            long sequence,
            ActivityDetailsSnapshot details,
            String comment,
            Instant createdAt,
            Instant modifiedAt
    ) {

        public ActivitySnapshot {
            if (id == null) {
                throw new IllegalArgumentException(
                        "El identificador de la Activity es obligatorio"
                );
            }

            if (type == null) {
                throw new IllegalArgumentException(
                        "El tipo de Activity es obligatorio"
                );
            }

            if (occurredAt == null) {
                throw new IllegalArgumentException(
                        "occurredAt es obligatorio"
                );
            }

            if (sequence < 0) {
                throw new IllegalArgumentException(
                        "sequence no puede ser negativo"
                );
            }

            if (details == null) {
                throw new IllegalArgumentException(
                        "Los detalles de la Activity son obligatorios"
                );
            }

            if (createdAt == null
                    || modifiedAt == null) {

                throw new IllegalArgumentException(
                        "Las fechas de la Activity son obligatorias"
                );
            }

            validateShape(
                    type,
                    assetReference,
                    details
            );
        }

        private static void validateShape(
                ActivityType type,
                AssetReferenceSnapshot assetReference,
                ActivityDetailsSnapshot details
        ) {
            switch (type) {

                case BUY, SELL -> {
                    requireAsset(
                            assetReference,
                            type
                    );

                    requireDetails(
                            details,
                            TradeDetailsSnapshot.class,
                            type
                    );
                }

                case DIVIDEND -> {
                    if (details
                            instanceof DividendUnitsDetailsSnapshot) {

                        requireAsset(
                                assetReference,
                                type
                        );

                    } else if (!(details
                            instanceof CashDetailsSnapshot)) {

                        throw invalidDetails(type);
                    }
                }

                case DEPOSIT,
                        WITHDRAW,
                        OPENING_CASH -> {

                    requireNoAsset(
                            assetReference,
                            type
                    );

                    requireDetails(
                            details,
                            CashDetailsSnapshot.class,
                            type
                    );
                }

                case EXCHANGE -> {
                    requireNoAsset(
                            assetReference,
                            type
                    );

                    requireDetails(
                            details,
                            ExchangeDetailsSnapshot.class,
                            type
                    );
                }

                case SPLIT -> {
                    requireAsset(
                            assetReference,
                            type
                    );

                    requireDetails(
                            details,
                            SplitDetailsSnapshot.class,
                            type
                    );
                }

                case OPENING_POSITION -> {
                    requireAsset(
                            assetReference,
                            type
                    );

                    requireDetails(
                            details,
                            OpeningPositionDetailsSnapshot.class,
                            type
                    );
                }
            }
        }

        private static void requireAsset(
                AssetReferenceSnapshot assetReference,
                ActivityType type
        ) {
            if (assetReference == null) {
                throw new IllegalArgumentException(
                        type + " requiere AssetReference"
                );
            }
        }

        private static void requireNoAsset(
                AssetReferenceSnapshot assetReference,
                ActivityType type
        ) {
            if (assetReference != null) {
                throw new IllegalArgumentException(
                        type + " no puede tener AssetReference"
                );
            }
        }

        private static <T extends ActivityDetailsSnapshot>
        void requireDetails(
                ActivityDetailsSnapshot details,
                Class<T> expected,
                ActivityType type
        ) {
            if (!expected.isInstance(details)) {
                throw invalidDetails(type);
            }
        }

        private static IllegalArgumentException invalidDetails(
                ActivityType type
        ) {
            return new IllegalArgumentException(
                    "Los detalles no son compatibles con ActivityType "
                            + type
            );
        }
    }

    public record ActivityAuditSnapshot(
            UUID id,
            UUID activityId,
            String beforeJson,
            String afterJson,
            String reason,
            Instant changedAt
    ) {

        public ActivityAuditSnapshot {
            if (id == null) {
                throw new IllegalArgumentException(
                        "El identificador del audit es obligatorio"
                );
            }

            if (activityId == null) {
                throw new IllegalArgumentException(
                        "El identificador de la Activity es obligatorio"
                );
            }

            if (beforeJson == null
                    || afterJson == null) {

                throw new IllegalArgumentException(
                        "Los snapshots del audit son obligatorios"
                );
            }

            if (changedAt == null) {
                throw new IllegalArgumentException(
                        "changedAt es obligatorio"
                );

            }

            if (reason != null) {
                reason = reason.trim();

                if (reason.length() > 500) {
                    throw new IllegalArgumentException(
                            "El motivo no puede superar los 500 caracteres"
                    );
                }
            }
        }
    }
}