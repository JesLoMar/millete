package com.puntomartinez.millete.dataexport.application.services;

import com.puntomartinez.millete.dataexport.domain.model.PeriodType;
import com.puntomartinez.millete.dataexport.domain.model.TransactionSnapshot;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.*;
import com.puntomartinez.millete.dataexport.infrastructure.out.fileexport.PdfBoxFileExportAdapter;
import com.puntomartinez.millete.dataexport.infrastructure.out.fileexport.ZipFileExportAdapter;
import com.puntomartinez.millete.investments.domain.model.*;
import com.puntomartinez.millete.investments.domain.ports.in.*;
import com.puntomartinez.millete.investments.domain.ports.out.AssetPriceRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetPriceRepository;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DataExportServiceTest {
    private static final Instant AS_OF = Instant.parse("2026-11-26T12:00:00Z");
    private final UUID userId = UUID.randomUUID();
    private final AssetReference asset = new AssetReference(AssetReferenceKind.SHARED, UUID.randomUUID());
    private final GetPortfolioAtUseCase portfolios = mock(GetPortfolioAtUseCase.class);
    private final ListClosedLotsUseCase closedLots = mock(ListClosedLotsUseCase.class);
    private final GetSharedAssetUseCase sharedAssets = mock(GetSharedAssetUseCase.class);
    private final GetInvestmentTrackingUseCase tracking = mock(GetInvestmentTrackingUseCase.class);
    private final AssetPriceRepository prices = mock(AssetPriceRepository.class);
    private final TransactionExportPort transactions = mock(TransactionExportPort.class);
    private DataExportService service;

    @BeforeEach
    void prepararExportaciones() {
        var csv = new ZipFileExportAdapter();
        var clock = mock(TimeProvider.class);
        when(clock.now()).thenReturn(AS_OF);
        when(portfolios.get(userId, AS_OF)).thenReturn(new GetPortfolioAtUseCase.PortfolioResult(
                AS_OF, "EUR", Map.of("EUR", new BigDecimal("10")),
                List.of(new GetPortfolioAtUseCase.PositionResult(
                        asset, new BigDecimal("3"), new BigDecimal("60"), new BigDecimal("270"),
                        new BigDecimal("210"), "EUR", CalculationStatus.CALCULABLE, false, false, null
                )), new BigDecimal("280"), false
        ));
        var shared = mock(SharedAsset.class, RETURNS_DEEP_STUBS);
        when(shared.getName()).thenReturn("Test Asset");
        when(shared.getSymbol()).thenReturn("TEST");
        when(shared.getCurrency()).thenReturn(CurrencyCode.of("USD"));
        when(shared.getType()).thenReturn(AssetType.STOCK);
        when(sharedAssets.getById(asset.id())).thenReturn(shared);
        when(tracking.getTrackingStartAt(userId)).thenReturn(Optional.of(AS_OF.minusSeconds(3600)));
        var price = mock(AssetPrice.class);
        when(price.valuationPrice()).thenReturn(new BigDecimal("100"));
        when(price.currency()).thenReturn(CurrencyCode.of("USD"));
        when(prices.findLatestAt(asset.id(), AS_OF)).thenReturn(Optional.of(price));
        when(closedLots.list(eq(userId), any(), eq(AS_OF))).thenAnswer(call -> {
            if (call.getArgument(1) == null) {
                throw new IllegalArgumentException("from y to son obligatorios.");
            }
            return List.of();
        });
        service = new DataExportService(
                mock(CategoryExportPort.class), transactions,
                mock(PlannedTransactionExportPort.class),
                mock(SavingsGoalExportPort.class), mock(UserPreferencesExportPort.class),
                csv, csv, new PdfBoxFileExportAdapter(), portfolios, closedLots, sharedAssets,
                mock(GetUserAssetUseCase.class), clock, tracking, prices, mock(UserAssetPriceRepository.class)
        );
    }

    @Test
    void omiteInversionesSoloDelJsonGeneral() throws Exception {
        var expense = new TransactionSnapshot(UUID.randomUUID(), userId, null, BigDecimal.TEN,
                LocalDate.of(2026, 11, 26), "EXPENSE", "Personal expense", AS_OF, AS_OF, true, "EUR", null);
        var transfer = new TransactionSnapshot(UUID.randomUUID(), userId, null, BigDecimal.ONE,
                expense.date(), "TRANSFER_OUT", "Transfer to investments", AS_OF, AS_OF, true,
                "EUR", UUID.randomUUID());
        when(transactions.findAllByUserId(userId)).thenReturn(List.of(expense, transfer));

        var mapper = new com.puntomartinez.millete.shared.infrastructure.config.JacksonConfig().objectMapper();
        var json = mapper.writeValueAsBytes(service.exportAllUserData(userId));
        var decoded = mapper.readValue(json, UserDataSnapshot.class);

        assertThat(decoded.investments()).isNull();
        assertThat(decoded.transactions()).containsExactly(expense);
        verifyNoInteractions(portfolios, closedLots, tracking, prices);

        String csv = new String(service.exportUserDataAsCsv(userId, "transactions"), StandardCharsets.UTF_8);
        assertThat(csv).contains("Personal expense", "Transfer to investments");
    }

    @Test
    void exportaElMismoInformeDePosicionesEnCsvYZip() throws Exception {
        String csv = new String(service.exportUserDataAsCsv(userId, "investments"), StandardCharsets.UTF_8);
        assertThat(csv).contains("Test Asset,POSITION,TEST,3,60,EUR,270,EUR,210,EUR,STOCK");
        try (var zip = new ZipInputStream(new ByteArrayInputStream(service.exportUserDataAsZip(userId)))) {
            boolean found = false;
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (entry.getName().equals("investments.csv")) {
                    assertThat(new String(zip.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo(csv);
                    found = true;
                }
            }
            assertThat(found).isTrue();
        }
        verify(closedLots, times(2)).list(userId, Instant.MIN, AS_OF);
        verifyNoInteractions(prices);
    }

    @ParameterizedTest
    @EnumSource(PeriodType.class)
    void pdfMuestraPreciosUnitariosSinHistorial(PeriodType period) throws Exception {
        try (var pdf = Loader.loadPDF(service.exportUserDataAsPdf(userId, period))) {
            assertThat(new PDFTextStripper().getText(pdf))
                    .contains("Test Asset", "LAST RECORDED PRICE", "AVERAGE PURCHASE PRICE", "USD 100.00", "EUR 20.00")
                    .doesNotContain("TEST", "270.00", "Investment Cash", "ACTIVE INVESTMENTS");
        }
        verifyNoInteractions(closedLots);
        verify(prices).findLatestAt(asset.id(), AS_OF);
        verifyNoMoreInteractions(prices);
    }

    @Test
    void exportaSinConfigurarInversiones() throws Exception {
        when(tracking.getTrackingStartAt(userId)).thenReturn(Optional.empty());
        assertThat(new String(service.exportUserDataAsCsv(userId, "categories"), StandardCharsets.UTF_8))
                .isEqualTo("name,budget_limit\r\n");
        assertThat(service.exportUserDataAsZip(userId)).isNotEmpty();
        try (var pdf = Loader.loadPDF(service.exportUserDataAsPdf(userId, PeriodType.ONE_MONTH))) {
            assertThat(new PDFTextStripper().getText(pdf))
                    .contains("No open positions.", "Transactions in Period");
        }
        verifyNoInteractions(portfolios, sharedAssets, closedLots, prices);
    }
}
