package com.puntomartinez.millete.dataexport.infrastructure.out.fileexport;

import com.puntomartinez.millete.dataexport.domain.model.PdfExportData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class HtmlPdfFileExportAdapterTest {

    private static final String XSS_PAYLOAD =
            "<script>alert('xss')</script><img src=x onerror=alert(1)>";

    private HtmlPdfFileExportAdapter adapter;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.addDialect(new TemporalsStubDialect());

        adapter = new HtmlPdfFileExportAdapter(engine);
    }

    @Test
    @DisplayName("renderHtml escapa HTML en campos controlados por el usuario (guardarraíl anti-XSS)")
    void renderHtmlShouldEscapeUserControlledFields() {
        String html = adapter.renderHtml(dataWithMaliciousInputs());

        assertThat(html).doesNotContain("<script>alert('xss')</script>");
        assertThat(html).doesNotContain("<img src=x onerror=alert(1)>");
        assertThat(html).contains("&lt;script&gt;");
    }

    @Test
    @DisplayName("renderHtml no incluye metas de ahorro en el PDF (decisión de producto)")
    void renderHtmlShouldNotIncludeSavingsGoals() {
        String html = adapter.renderHtml(dataWithMaliciousInputs());

        assertThat(html).doesNotContain("Viaje a Japón");
    }

    @Test
    @DisplayName("generatePdf produce un PDF válido aunque los datos contengan HTML")
    void generatePdfShouldSucceedWithHtmlInData() {
        byte[] pdf = adapter.generatePdf(dataWithMaliciousInputs());

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    private PdfExportData dataWithMaliciousInputs() {
        PdfExportData.Summary summary = new PdfExportData.Summary(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 1,
                "—", BigDecimal.ZERO, 0.0,
                BigDecimal.ZERO, 0, 1, BigDecimal.ZERO);

        PdfExportData.TransactionRow tx = new PdfExportData.TransactionRow(
                LocalDateTime.now(), "Categoría", XSS_PAYLOAD, "Gasto", new BigDecimal("10.00"));

        PdfExportData.SavingsGoalRow goal = new PdfExportData.SavingsGoalRow(
                "Viaje a Japón", new BigDecimal("3000.00"), new BigDecimal("1500.00"),
                50.0, LocalDate.now().plusMonths(6), "Alta", "En progreso", null);

        return new PdfExportData("1 month", LocalDate.now().minusMonths(1), LocalDate.now(),
                summary, List.of(tx), List.of(), List.of(goal));
    }

    private static final class TemporalsStubDialect implements IExpressionObjectDialect {

        @Override
        public String getName() {
            return "temporals-stub";
        }

        @Override
        public IExpressionObjectFactory getExpressionObjectFactory() {
            return new IExpressionObjectFactory() {
                @Override
                public Set<String> getAllExpressionObjectNames() {
                    return Set.of("temporals");
                }

                @Override
                public Object buildObject(IExpressionContext context, String expressionObjectName) {
                    return new TemporalsStub();
                }

                @Override
                public boolean isCacheable(String expressionObjectName) {
                    return true;
                }
            };
        }
    }

    @SuppressWarnings("unused")
    public static final class TemporalsStub {
        public String format(TemporalAccessor temporal, String pattern) {
            return DateTimeFormatter.ofPattern(pattern).format(temporal);
        }
    }
}