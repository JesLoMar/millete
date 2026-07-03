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

                // Verify that no cell starts with a formula prefix (without the sanitizing quote)
                // The sanitized values start with ', so we check that the raw formula prefix at start of line/cell doesn't exist
                String[] lines = content.split("\n");
                for (String line : lines) {
                    String[] cells = line.split(",");
                    for (String cell : cells) {
                        String trimmed = cell.trim();
                        if (!trimmed.isEmpty() && !trimmed.equals("name") && !trimmed.equals("budget_limit")
                                && !trimmed.equals("category_name") && !trimmed.equals("amount")
                                && !trimmed.equals("date") && !trimmed.equals("type") && !trimmed.equals("description")
                                && !trimmed.equals("frequency_type") && !trimmed.equals("frequency_interval")
                                && !trimmed.equals("start_date") && !trimmed.equals("end_date")
                                && !trimmed.equals("last_executed_date") && !trimmed.equals("asset_name")
                                && !trimmed.equals("ticker") && !trimmed.equals("quantity")
                                && !trimmed.equals("purchase_price") && !trimmed.equals("current_price")
                                && !trimmed.equals("purchase_date") && !trimmed.equals("target_amount")
                                && !trimmed.equals("current_amount") && !trimmed.equals("progress")
                                && !trimmed.equals("deadline") && !trimmed.equals("priority")
                                && !trimmed.equals("status") && !trimmed.equals("link")) {
                            // Check that if the cell starts with a formula prefix, it is preceded by '
                            if (trimmed.startsWith("=") || trimmed.startsWith("+") || trimmed.startsWith("-")
                                    || trimmed.startsWith("@")) {
                                assertTrue(trimmed.startsWith("'"), entry.getName() + " contiene fórmula sin sanitizar: " + trimmed);
                            }
                        }
                    }
                }
            }
        }
    }
}
