package com.puntomartinez.millete.dataexport.infrastructure.out.fileexport;

import com.puntomartinez.millete.dataexport.domain.model.ExportData;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

class ZipFileExportAdapterTest {

    private final ZipFileExportAdapter adapter = new ZipFileExportAdapter();

    @Test
    void generateCsv_shouldSanitizeFormulaInjectionInDescription() {
        String maliciousDescription = "=CMD|' /C calc'!A0";
        ExportData data = new ExportData(
                null,
                List.of(new ExportData.TransactionExportRow(
                        "Food",
                        new BigDecimal("50.00"),
                        LocalDateTime.now(),
                        "EXPENSE",
                        maliciousDescription
                )),
                null,
                null,
                null
        );

        byte[] csvBytes = adapter.generateCsv(data, "transactions");
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("'=CMD|' /C calc'!A0"), "La fórmula maliciosa debe ir precedida de una comilla simple");
        assertFalse(csv.contains("," + maliciousDescription), "La fórmula no debe aparecer sin sanitizar");
    }

    @Test
    void generateCsv_shouldNotModifySafeText() {
        ExportData data = new ExportData(
                List.of(new ExportData.CategoryExportRow("Groceries", new BigDecimal("300.00"))),
                null,
                null,
                null,
                null
        );

        byte[] csvBytes = adapter.generateCsv(data, "categories");
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("Groceries"));
        assertFalse(csv.contains("'Groceries"));
    }

    @Test
    void generateCsv_shouldSanitizeFormulaPrefixes() {
        ExportData data = new ExportData(
                List.of(
                        new ExportData.CategoryExportRow("=SUM(A1:A10)", new BigDecimal("100.00")),
                        new ExportData.CategoryExportRow("+123456789", new BigDecimal("200.00")),
                        new ExportData.CategoryExportRow("-100", new BigDecimal("300.00")),
                        new ExportData.CategoryExportRow("@SUM(A1)", new BigDecimal("400.00"))
                ),
                null,
                null,
                null,
                null
        );

        byte[] csvBytes = adapter.generateCsv(data, "categories");
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("'=SUM(A1:A10)"));
        assertTrue(csv.contains("'+123456789"));
        assertTrue(csv.contains("'-100"));
        assertTrue(csv.contains("'@SUM(A1)"));
    }

    @Test
    void generateZip_shouldSanitizeAllTextFields() throws Exception {
        ExportData data = new ExportData(
                List.of(new ExportData.CategoryExportRow("=MALICIOUS()", new BigDecimal("100.00"))),
                List.of(new ExportData.TransactionExportRow(
                        "+123456789",
                        new BigDecimal("50.00"),
                        LocalDateTime.now(),
                        "@TYPE",
                        "=CMD|' /C calc'!A0"
                )),
                List.of(new ExportData.PlannedTransactionExportRow(
                        "-EVIL",
                        new BigDecimal("10.00"),
                        "=TYPE",
                        "@DESC",
                        "+FREQ",
                        1,
                        LocalDate.now(),
                        LocalDate.now().plusDays(1),
                        null
                )),
                List.of(new ExportData.InvestmentExportRow(
                        "=ASSET",
                        "@TICK",
                        new BigDecimal("1.00"),
                        new BigDecimal("100.00"),
                        new BigDecimal("110.00"),
                        "=TYPE",
                        LocalDateTime.now()
                )),
                List.of(new ExportData.SavingsGoalExportRow(
                        "=GOAL",
                        new BigDecimal("1000.00"),
                        new BigDecimal("100.00"),
                        10.0,
                        LocalDate.now().plusMonths(1),
                        "+HIGH",
                        "@ACTIVE",
                        "=LINK"
                ))
        );

        byte[] zipBytes = adapter.generateZip(data);

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            while (true) {
                var entry = zis.getNextEntry();
                if (entry == null) {
                    break;
                }

                String content = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));

                assertFalse(content.contains("=MALICIOUS()"), entry.getName() + " contiene fórmula sin sanitizar");
                assertFalse(content.contains("+123456789"), entry.getName() + " contiene fórmula sin sanitizar");
                assertFalse(content.contains("-EVIL"), entry.getName() + " contiene fórmula sin sanitizar");
                assertFalse(content.contains("@TYPE"), entry.getName() + " contiene fórmula sin sanitizar");
                assertFalse(content.contains("=CMD|' /C calc'!A0"), entry.getName() + " contiene fórmula sin sanitizar");
            }
        }
    }
}
