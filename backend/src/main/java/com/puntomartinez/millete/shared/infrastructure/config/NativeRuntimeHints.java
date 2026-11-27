package com.puntomartinez.millete.shared.infrastructure.config;

import com.puntomartinez.millete.assistant.infrastructure.out.ai.client.dto.StructuredChatCompletionRequestDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.client.dto.StructuredChatCompletionResponseDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.AddCategoryExtractionDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.AddRecurringTransactionExtractionDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.AddSavingsGoalContributionExtractionDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.AddSavingsGoalExtractionDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.AddTransactionExtractionDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.EditRecurringTransactionExtractionDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.EditSavingsGoalExtractionDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.EditTransactionExtractionDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.verdict.dto.VerdictDecisionRequestDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.verdict.dto.VerdictDecisionResponseDTO;
import com.puntomartinez.millete.dataexport.domain.model.InvestmentLedgerSnapshotDeserializer;
import com.puntomartinez.millete.dataexport.domain.model.PdfExportData;
import com.puntomartinez.millete.investments.domain.model.CashActivityDetails;
import com.puntomartinez.millete.investments.domain.model.DividendUnitsDetails;
import com.puntomartinez.millete.investments.domain.model.ExchangeActivityDetails;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot;
import com.puntomartinez.millete.investments.domain.model.OpeningPositionDetails;
import com.puntomartinez.millete.investments.domain.model.SplitActivityDetails;
import com.puntomartinez.millete.investments.domain.model.TradeActivityDetails;
import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

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

        hints.resources().registerPattern("db/migration/*.sql");
        registerJsonBindingHints(hints);

        // JJWT 0.12.6 instancia el constructor del generador de tokens por nombre.
        hints.reflection().registerType(
                TypeReference.of("io.jsonwebtoken.impl.DefaultJwtBuilder"),
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS
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

    private void registerJsonBindingHints(RuntimeHints hints) {
        var registrar = new BindingReflectionHintsRegistrar();

        // Jackson lee y escribe estos detalles en persistencia, fuera de los controladores.
        registrar.registerReflectionHints(
                hints.reflection(),
                TradeActivityDetails.class,
                CashActivityDetails.class,
                DividendUnitsDetails.class,
                SplitActivityDetails.class,
                ExchangeActivityDetails.class,
                OpeningPositionDetails.class
        );

        // RestClient y los extractores del asistente enlazan estos DTO fuera de MVC.
        registrar.registerReflectionHints(
                hints.reflection(),
                StructuredChatCompletionRequestDTO.class,
                StructuredChatCompletionResponseDTO.class,
                VerdictDecisionRequestDTO.class,
                VerdictDecisionResponseDTO.class,
                AddCategoryExtractionDTO.class,
                AddRecurringTransactionExtractionDTO.class,
                AddSavingsGoalContributionExtractionDTO.class,
                AddSavingsGoalExtractionDTO.class,
                AddTransactionExtractionDTO.class,
                EditRecurringTransactionExtractionDTO.class,
                EditSavingsGoalExtractionDTO.class,
                EditTransactionExtractionDTO.class
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
