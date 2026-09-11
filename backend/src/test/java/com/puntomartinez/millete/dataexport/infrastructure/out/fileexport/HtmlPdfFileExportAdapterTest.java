package com.puntomartinez.millete.dataexport.infrastructure.out.fileexport;

import com.puntomartinez.millete.dataexport.domain.model.PdfExportData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlPdfFileExportAdapterTest {

    private static final String XSS_PAYLOAD =
            "<script>alert('xss')</script><img src=x onerror=alert(1)>";

    private HtmlPdfFileExportAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new HtmlPdfFileExportAdapter();
    }

    @Test
    @DisplayName("generatePdf produce un PDF válido con datos controlados por el usuario")
    void generatePdfShouldSucceedWithHtmlInData() {
        byte[] pdf = adapter.generatePdf(dataWithMaliciousInputs());

        assertThat(pdf)
                .isNotEmpty()
                .startsWith("%PDF-".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    }

    @Test
    @DisplayName("generatePdf no incorpora etiquetas HTML sin procesar")
    void generatePdfShouldNotContainRawHtml() {
        byte[] pdf = adapter.generatePdf(dataWithMaliciousInputs());

        String pdfContent = new String(
                pdf,
                java.nio.charset.StandardCharsets.ISO_8859_1
        );

        assertThat(pdfContent)
                .doesNotContain("<script>")
                .doesNotContain("<img")
                .doesNotContain("onerror=");
    }

    @Test
    @DisplayName("generatePdf no incluye metas de ahorro")
    void generatePdfShouldNotIncludeSavingsGoals() {
        byte[] pdf = adapter.generatePdf(dataWithMaliciousInputs());

        String pdfContent = new String(
                pdf,
                java.nio.charset.StandardCharsets.ISO_8859_1
        );

        assertThat(pdfContent)
                .doesNotContain("Viaje a Japón");
    }

    private PdfExportData dataWithMaliciousInputs() {
        PdfExportData.Summary summary = new PdfExportData.Summary(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                1,
                "—",
                BigDecimal.ZERO,
                0.0,
                BigDecimal.ZERO,
                0,
                1,
                BigDecimal.ZERO
        );

        PdfExportData.TransactionRow tx = new PdfExportData.TransactionRow(
                LocalDateTime.now(),
                "Categoría",
                XSS_PAYLOAD,
                "Gasto",
                new BigDecimal("10.00")
        );

        PdfExportData.SavingsGoalRow goal = new PdfExportData.SavingsGoalRow(
                "Viaje a Japón",
                new BigDecimal("3000.00"),
                new BigDecimal("1500.00"),
                50.0,
                LocalDate.now().plusMonths(6),
                "Alta",
                "En progreso",
                null
        );

        return new PdfExportData(
                "1 month",
                LocalDate.now().minusMonths(1),
                LocalDate.now(),
                summary,
                List.of(tx),
                List.of(),
                List.of(goal)
        );
    }
}