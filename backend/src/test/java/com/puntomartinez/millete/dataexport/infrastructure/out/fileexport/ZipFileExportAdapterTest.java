package com.puntomartinez.millete.dataexport.infrastructure.out.fileexport;

import com.puntomartinez.millete.dataexport.domain.model.ExportData;
import org.junit.jupiter.api.DisplayName;
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

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ZipFileExportAdapter")
class ZipFileExportAdapterTest {

    private final ZipFileExportAdapter adapter = new ZipFileExportAdapter();

    @Test
    @DisplayName("generateCsv should sanitize formula injection in description")
    void generateCsvShouldSanitizeFormulaInjection() {
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
                null, null, null
        );

        byte[] csvBytes = adapter.generateCsv(data, "transactions");
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertThat(csv).contains("'=CMD|' /C calc'!A0");
        assertThat(csv).doesNotContain("," + maliciousDescription);
    }

    @Test
    @DisplayName("generateCsv should not modify safe text")
    void generateCsvShouldNotModifySafeText() {
        ExportData data = new ExportData(
                List.of(new ExportData.CategoryExportRow(
                        "Groceries", new BigDecimal("300.00")
                )),
                null, null, null, null
        );

        byte[] csvBytes = adapter.generateCsv(data, "categories");
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertThat(csv).contains("Groceries");
        assertThat(csv).doesNotContain("'Groceries");
    }

    @Test
    @DisplayName("generateCsv should sanitize formula prefixes")
    void generateCsvShouldSanitizeFormulaPrefixes() {
        ExportData data = new ExportData(
                List.of(
                        new ExportData.CategoryExportRow("=SUM(A1:A10)", new BigDecimal("100.00")),
                        new ExportData.CategoryExportRow("+123456789", new BigDecimal("200.00")),
                        new ExportData.CategoryExportRow("-100", new BigDecimal("300.00")),
                        new ExportData.CategoryExportRow("@SUM(A1)", new BigDecimal("400.00"))
                ),
                null, null, null, null
        );

        byte[] csvBytes = adapter.generateCsv(data, "categories");
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertThat(csv).contains("'=SUM(A1:A10)");
        assertThat(csv).contains("'+123456789");
        assertThat(csv).contains("'-100");
        assertThat(csv).contains("'@SUM(A1)");
    }

    @Test
    @DisplayName("generateZip should sanitize all text fields")
    void generateZipShouldSanitizeAllTextFields() throws Exception {
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
                        "=LINK"
                ))
        );

        byte[] zipBytes = adapter.generateZip(data);

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            while (true) {
                var entry = zis.getNextEntry();
                if (entry == null) break;

                String content = new BufferedReader(
                        new InputStreamReader(zis, StandardCharsets.UTF_8)
                ).lines().collect(Collectors.joining("\n"));

                String[] lines = content.split("\n");
                for (String line : lines) {
                    String[] cells = line.split(",");
                    for (String cell : cells) {
                        String trimmed = cell.trim();
                        if (!trimmed.isEmpty() && isDataField(trimmed)) {
                            if (trimmed.startsWith("=") || trimmed.startsWith("+")
                                    || trimmed.startsWith("-") || trimmed.startsWith("@")) {
                                assertThat(trimmed)
                                        .as(entry.getName() + " contains unsanitized formula: " + trimmed)
                                        .startsWith("'");
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isDataField(String value) {
        return !value.equals("name") && !value.equals("budget_limit")
                && !value.equals("category_name") && !value.equals("amount")
                && !value.equals("date") && !value.equals("type")
                && !value.equals("description") && !value.equals("frequency_type")
                && !value.equals("frequency_interval") && !value.equals("start_date")
                && !value.equals("end_date") && !value.equals("last_executed_date")
                && !value.equals("asset_name") && !value.equals("ticker")
                && !value.equals("quantity") && !value.equals("purchase_price")
                && !value.equals("current_price") && !value.equals("purchase_date")
                && !value.equals("target_amount") && !value.equals("current_amount")
                && !value.equals("progress") && !value.equals("deadline")
                && !value.equals("priority") && !value.equals("link");
    }
}