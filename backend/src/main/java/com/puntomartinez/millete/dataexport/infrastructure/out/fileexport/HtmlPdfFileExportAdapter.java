package com.puntomartinez.millete.dataexport.infrastructure.out.fileexport;

import com.puntomartinez.millete.dataexport.domain.model.ExportData;
import com.puntomartinez.millete.dataexport.domain.model.PdfExportData;
import com.puntomartinez.millete.dataexport.domain.ports.out.FileExportPort;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

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
            Context context = new Context();
            context.setVariable("periodDisplayName", data.periodDisplayName());
            context.setVariable("startDate", data.startDate());
            context.setVariable("endDate", data.endDate());
            context.setVariable("summary", data.summary());
            context.setVariable("transactions", data.transactions());
            context.setVariable("investments", data.investments());

            String html = templateEngine.process("export-pdf", context);

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
}