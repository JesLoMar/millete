package com.puntomartinez.millete.shared.infrastructure.config;

import com.puntomartinez.millete.dataexport.domain.model.InvestmentLedgerSnapshotDeserializer;
import com.puntomartinez.millete.dataexport.domain.model.PdfExportData;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import java.util.UUID;

public class NativeRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(
            RuntimeHints hints,
            ClassLoader classLoader
    ) {

        hints.reflection().registerType(
                UUID[].class
        );

        hints.resources().registerPattern(
                "org/apache/pdfbox/resources/afm/*.afm"
        );

        hints.resources().registerPattern(
                "org/apache/pdfbox/resources/glyphlist/glyphlist.txt"
        );

        hints.resources().registerPattern(
                "org/apache/pdfbox/resources/glyphlist/zapfdingbats.txt"
        );

        hints.reflection().registerType(
                PdfExportData.Summary.class,
                MemberCategory.INVOKE_PUBLIC_METHODS
        );

        hints.reflection().registerType(
                PdfExportData.InvestmentRow.class,
                MemberCategory.INVOKE_PUBLIC_METHODS
        );

        hints.reflection().registerType(
                PdfExportData.TransactionRow.class,
                MemberCategory.INVOKE_PUBLIC_METHODS
        );

        hints.reflection().registerType(
                PdfExportData.TransferRow.class,
                MemberCategory.INVOKE_PUBLIC_METHODS
        );

        hints.reflection().registerType(
                PdfExportData.CashRow.class,
                MemberCategory.INVOKE_PUBLIC_METHODS
        );

        registerSnapshotType(
                hints,
                InvestmentLedgerSnapshot.class
        );

        registerSnapshotType(
                hints,
                InvestmentLedgerSnapshot.AssetReferenceSnapshot.class
        );

        registerSnapshotType(
                hints,
                InvestmentLedgerSnapshot.MoneySnapshot.class
        );

        registerSnapshotType(
                hints,
                InvestmentLedgerSnapshot.AppliedFxRateSnapshot.class
        );

        registerSnapshotType(
                hints,
                InvestmentLedgerSnapshot.AssetSectorSnapshot.class
        );

        registerSnapshotType(
                hints,
                InvestmentLedgerSnapshot.UserAssetSnapshot.class
        );

        registerSnapshotType(
                hints,
                InvestmentLedgerSnapshot.UserAssetPriceSnapshot.class
        );

        registerSnapshotType(
                hints,
                InvestmentLedgerSnapshot.HoldingSnapshot.class
        );

        registerSnapshotType(
                hints,
                InvestmentLedgerSnapshot.ActivitySnapshot.class
        );

        registerSnapshotType(
                hints,
                InvestmentLedgerSnapshot.ActivityAuditSnapshot.class
        );

        registerSnapshotType(
                hints,
                InvestmentLedgerSnapshot.TradeDetailsSnapshot.class
        );

        registerSnapshotType(
                hints,
                InvestmentLedgerSnapshot.CashDetailsSnapshot.class
        );

        registerSnapshotType(
                hints,
                InvestmentLedgerSnapshot.DividendUnitsDetailsSnapshot.class
        );

        registerSnapshotType(
                hints,
                InvestmentLedgerSnapshot.ExchangeDetailsSnapshot.class
        );

        registerSnapshotType(
                hints,
                InvestmentLedgerSnapshot.SplitDetailsSnapshot.class
        );

        registerSnapshotType(
                hints,
                InvestmentLedgerSnapshot.OpeningPositionDetailsSnapshot.class
        );

        hints.reflection().registerType(
                InvestmentLedgerSnapshotDeserializer.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_METHODS
        );
    }

    private void registerSnapshotType(
            RuntimeHints hints,
            Class<?> type
    ) {
        hints.reflection().registerType(
                type,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_METHODS
        );
    }
}