package com.puntomartinez.millete.dataexport.infrastructure.out.fileexport;

import com.puntomartinez.millete.dataexport.domain.model.ExportData;
import com.puntomartinez.millete.dataexport.domain.model.PdfExportData;
import com.puntomartinez.millete.dataexport.domain.ports.out.FileExportPort;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;

@Component("pdfFileExportAdapter")
public class HtmlPdfFileExportAdapter implements FileExportPort {

    private final TemplateEngine templateEngine;

    public HtmlPdfFileExportAdapter(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public byte[] generateZip(ExportData exportData) {
        return new byte[0];
    }

    @Override
    public byte[] generateCsv(ExportData exportData, String entityType) {
        return new byte[0];
    }

    @Override
    public byte[] generatePdf(PdfExportData data) {
        try {
            String html = renderHtml(data);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(baos);
            renderer.finishPDF();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF con Flying Saucer", e);
        }
    }

    /**
     * Renderiza la plantilla export-pdf con los datos del informe.
     *
     * CONTRATO DE SEGURIDAD: todos los campos controlados por el usuario
     * (descripciones, nombres, tickers...) llegan aquí en crudo y la plantilla
     * los renderiza SIEMPRE con th:text (escape HTML por defecto).
     * No usar th:utext en esta plantilla: inyectaría HTML sin escapar.
     * HtmlPdfFileExportAdapterTest protege este contrato.
     *
     * NOTA: las metas de ahorro NO se incluyen en el PDF por decisión de
     * producto (solo información financiera) — no añadir "savingsGoals"
     * al contexto.
     */
    String renderHtml(PdfExportData data) {
        Context context = new Context();
        context.setVariable("periodDisplayName", data.periodDisplayName());
        context.setVariable("startDate", data.startDate());
        context.setVariable("endDate", data.endDate());
        context.setVariable("summary", data.summary());
        context.setVariable("transactions", data.transactions());
        context.setVariable("investments", data.investments());
        return templateEngine.process("export-pdf", context);
    }
}