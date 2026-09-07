package com.puntomartinez.millete.dataexport.infrastructure.out.fileexport;

import com.puntomartinez.millete.dataexport.domain.model.ExportData;
import com.puntomartinez.millete.dataexport.domain.model.PdfExportData;
import com.puntomartinez.millete.dataexport.domain.ports.out.FileExportPort;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component("pdfFileExportAdapter")
public class HtmlPdfFileExportAdapter implements FileExportPort {

    private static final PDRectangle PAGE_SIZE = PDRectangle.A4;

    private static final float PAGE_MARGIN = 28f;
    private static final float CONTENT_WIDTH =
            PAGE_SIZE.getWidth() - (PAGE_MARGIN * 2);

    private static final float HEADER_HEIGHT = 72f;

    // Espaciados verticales reutilizables para mantener un ritmo consistente
    private static final float SPACE_AFTER_HEADER = 20f;
    private static final float SPACE_AFTER_SUMMARY = 26f;
    private static final float SPACE_TITLE_TO_DIVIDER = 8f;
    private static final float SPACE_DIVIDER_TO_CONTENT = 16f;
    private static final float SPACE_BETWEEN_SECTIONS = 30f;
    private static final float TABLE_HEADER_HEIGHT = 22f;
    private static final float TABLE_ROW_HEIGHT = 22f;

    private static final PDType1Font FONT_REGULAR =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private static final PDType1Font FONT_BOLD =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private static final PDType1Font FONT_ITALIC =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

    private static final int TEXT_COLOR_R = 61;
    private static final int TEXT_COLOR_G = 43;
    private static final int TEXT_COLOR_B = 31;

    private static final int MUTED_COLOR_R = 140;
    private static final int MUTED_COLOR_G = 123;
    private static final int MUTED_COLOR_B = 107;

    private static final int BACKGROUND_R = 247;
    private static final int BACKGROUND_G = 228;
    private static final int BACKGROUND_B = 197;

    private static final int CARD_R = 255;
    private static final int CARD_G = 242;
    private static final int CARD_B = 217;

    private static final int GREEN_R = 27;
    private static final int GREEN_G = 77;
    private static final int GREEN_B = 62;

    private static final int RED_R = 194;
    private static final int RED_G = 59;
    private static final int RED_B = 34;

    private static final int ORANGE_R = 192;
    private static final int ORANGE_G = 96;
    private static final int ORANGE_B = 24;

    private static final int LINE_R = 217;
    private static final int LINE_G = 200;
    private static final int LINE_B = 160;

    private static final int ALTERNATE_ROW_R = 245;
    private static final int ALTERNATE_ROW_G = 222;
    private static final int ALTERNATE_ROW_B = 179;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm",
                    Locale.ENGLISH
            );

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
        try (
                PDDocument document = new PDDocument();
                ByteArrayOutputStream output = new ByteArrayOutputStream()
        ) {
            PDPage page = new PDPage(PAGE_SIZE);
            document.addPage(page);

            try (PDPageContentStream content =
                         new PDPageContentStream(document, page)) {

                drawBackground(content);
                drawHeader(content, data);
                float summaryBottomY = drawSummary(content, data.summary());

                PdfPageContext context = new PdfPageContext(
                        document,
                        content
                );

                float y = summaryBottomY - SPACE_AFTER_SUMMARY;

                if (!data.investments().isEmpty()) {
                    y = drawInvestments(
                            context,
                            data.investments(),
                            y
                    );
                } else {
                    y = drawSectionTitle(
                            context.content(),
                            "Active Investments",
                            y
                    );

                    drawEmptyMessage(
                            context.content(),
                            "No active investments at this time.",
                            y - SPACE_DIVIDER_TO_CONTENT
                    );

                    y -= SPACE_DIVIDER_TO_CONTENT + 30;
                }

                y = drawSectionTitle(
                        context.content(),
                        "Transactions in Period",
                        y - SPACE_BETWEEN_SECTIONS
                );

                if (!data.transactions().isEmpty()) {
                    drawTransactions(
                            context,
                            data.transactions(),
                            y - SPACE_DIVIDER_TO_CONTENT
                    );
                } else {
                    drawEmptyMessage(
                            context.content(),
                            "No transactions in this period.",
                            y - SPACE_DIVIDER_TO_CONTENT
                    );
                }
            }

            document.save(output);
            return output.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error generando PDF con Apache PDFBox",
                    e
            );
        }
    }

    private static void drawBackground(
            PDPageContentStream content
    ) throws IOException {

        setNonStrokingColor(
                content,
                BACKGROUND_R,
                BACKGROUND_G,
                BACKGROUND_B
        );

        content.addRect(
                0,
                0,
                PAGE_SIZE.getWidth(),
                PAGE_SIZE.getHeight()
        );

        content.fill();
    }

    private void drawHeader(
            PDPageContentStream content,
            PdfExportData data
    ) throws IOException {

        float x = PAGE_MARGIN;
        float y = PAGE_SIZE.getHeight() - PAGE_MARGIN;

        setNonStrokingColor(
                content,
                CARD_R,
                CARD_G,
                CARD_B
        );

        content.addRect(
                x,
                y - HEADER_HEIGHT,
                CONTENT_WIDTH,
                HEADER_HEIGHT
        );

        content.fill();

        setNonStrokingColor(
                content,
                ORANGE_R,
                ORANGE_G,
                ORANGE_B
        );

        content.addRect(
                x,
                y - HEADER_HEIGHT,
                4,
                HEADER_HEIGHT
        );

        content.fill();

        drawText(
                content,
                "Millete - Financial Data",
                FONT_BOLD,
                20,
                x + 20,
                y - 30,
                TEXT_COLOR_R,
                TEXT_COLOR_G,
                TEXT_COLOR_B
        );

        String period = String.format(
                Locale.ENGLISH,
                "Period: %s — %s (%s)",
                data.startDate(),
                data.endDate(),
                data.periodDisplayName()
        );

        drawText(
                content,
                period,
                FONT_REGULAR,
                12,
                x + 20,
                y - 50,
                MUTED_COLOR_R,
                MUTED_COLOR_G,
                MUTED_COLOR_B
        );
    }

    private float drawSummary(
            PDPageContentStream content,
            PdfExportData.Summary summary
    ) throws IOException {

        float y =
                PAGE_SIZE.getHeight()
                        - PAGE_MARGIN
                        - HEADER_HEIGHT
                        - SPACE_AFTER_HEADER;

        float gap = 8;
        float cardWidth =
                (CONTENT_WIDTH - (gap * 3)) / 4;

        float cardHeight = 68;
        float rowGap = 8;

        drawMetricCard(
                content,
                PAGE_MARGIN,
                y,
                cardWidth,
                cardHeight,
                "BALANCE",
                formatSignedEuro(summary.balance()),
                summary.balance().compareTo(BigDecimal.ZERO) >= 0
                        ? GREEN_R
                        : RED_R,
                summary.balance().compareTo(BigDecimal.ZERO) >= 0
                        ? GREEN_G
                        : RED_G,
                summary.balance().compareTo(BigDecimal.ZERO) >= 0
                        ? GREEN_B
                        : RED_B
        );

        drawMetricCard(
                content,
                PAGE_MARGIN + cardWidth + gap,
                y,
                cardWidth,
                cardHeight,
                "INCOME",
                "+" + formatEuro(summary.totalIncome()),
                GREEN_R,
                GREEN_G,
                GREEN_B
        );

        drawMetricCard(
                content,
                PAGE_MARGIN + ((cardWidth + gap) * 2),
                y,
                cardWidth,
                cardHeight,
                "EXPENSES",
                "-" + formatEuro(summary.totalExpenses()),
                RED_R,
                RED_G,
                RED_B
        );

        drawMetricCard(
                content,
                PAGE_MARGIN + ((cardWidth + gap) * 3),
                y,
                cardWidth,
                cardHeight,
                "TRANSACTIONS",
                String.valueOf(summary.transactionCount()),
                TEXT_COLOR_R,
                TEXT_COLOR_G,
                TEXT_COLOR_B
        );

        y -= cardHeight + rowGap;

        drawMetricCard(
                content,
                PAGE_MARGIN,
                y,
                cardWidth,
                cardHeight,
                "INVESTMENTS VALUE",
                formatEuro(summary.investmentsTotalValue()),
                TEXT_COLOR_R,
                TEXT_COLOR_G,
                TEXT_COLOR_B
        );

        drawMetricCard(
                content,
                PAGE_MARGIN + cardWidth + gap,
                y,
                cardWidth,
                cardHeight,
                "ACTIVE INVESTMENTS",
                String.valueOf(summary.activeInvestmentsCount()),
                TEXT_COLOR_R,
                TEXT_COLOR_G,
                TEXT_COLOR_B
        );

        drawMetricCard(
                content,
                PAGE_MARGIN + ((cardWidth + gap) * 2),
                y,
                cardWidth,
                cardHeight,
                "SAVINGS GOALS",
                String.valueOf(summary.activeSavingsGoalsCount()),
                TEXT_COLOR_R,
                TEXT_COLOR_G,
                TEXT_COLOR_B
        );

        drawMetricCard(
                content,
                PAGE_MARGIN + ((cardWidth + gap) * 3),
                y,
                cardWidth,
                cardHeight,
                "TOTAL SAVED",
                formatEuro(summary.totalSavedAmount()),
                GREEN_R,
                GREEN_G,
                GREEN_B
        );

        return y - cardHeight;
    }

    private void drawMetricCard(
            PDPageContentStream content,
            float x,
            float y,
            float width,
            float height,
            String label,
            String value,
            int valueR,
            int valueG,
            int valueB
    ) throws IOException {

        setNonStrokingColor(
                content,
                CARD_R,
                CARD_G,
                CARD_B
        );

        content.addRect(
                x,
                y - height,
                width,
                height
        );

        content.fill();

        drawText(
                content,
                label,
                FONT_BOLD,
                7,
                x + 12,
                y - 19,
                MUTED_COLOR_R,
                MUTED_COLOR_G,
                MUTED_COLOR_B
        );

        drawText(
                content,
                value,
                FONT_BOLD,
                15,
                x + 12,
                y - 45,
                valueR,
                valueG,
                valueB
        );
    }

    private float drawInvestments(
            PdfPageContext context,
            List<PdfExportData.InvestmentRow> investments,
            float y
    ) throws IOException {

        y = drawSectionTitle(
                context.content(),
                "Active Investments",
                y
        );

        String[] headers = {
                "Asset",
                "Ticker",
                "Type",
                "Quantity",
                "Purchase Price",
                "Current Price",
                "Current Value",
                "Return"
        };

        float[] widths = {
                74,
                42,
                50,
                48,
                70,
                70,
                70,
                50
        };

        float tableY = y - SPACE_DIVIDER_TO_CONTENT;

        drawTableHeader(
                context.content(),
                tableY,
                headers,
                widths
        );

        float rowY = tableY - TABLE_HEADER_HEIGHT;
        boolean alternate = false;

        for (PdfExportData.InvestmentRow investment : investments) {

            if (rowY < 55) {
                context.startNewPage();

                rowY =
                        PAGE_SIZE.getHeight()
                                - PAGE_MARGIN
                                - TABLE_HEADER_HEIGHT;

                drawTableHeader(
                        context.content(),
                        rowY,
                        headers,
                        widths
                );

                rowY -= TABLE_HEADER_HEIGHT;
            }

            drawTableRowBackground(
                    context.content(),
                    rowY,
                    widths,
                    alternate
            );

            String returnValue =
                    (investment.returnPercentage() >= 0
                            ? "+"
                            : "")
                            + formatDecimal(
                            investment.returnPercentage()
                    )
                            + "%";

            String[] values = {
                    safe(investment.assetName()),
                    safe(investment.ticker()),
                    safe(investment.type()),
                    formatDecimal(investment.quantity(), 4),
                    formatEuro(investment.purchasePrice()),
                    formatEuro(investment.currentPrice()),
                    formatEuro(investment.currentValue()),
                    returnValue
            };

            drawTableRow(
                    context.content(),
                    rowY,
                    values,
                    widths
            );

            rowY -= TABLE_ROW_HEIGHT;
            alternate = !alternate;
        }

        return rowY;
    }

    private void drawTransactions(
            PdfPageContext context,
            List<PdfExportData.TransactionRow> transactions,
            float y
    ) throws IOException {

        String[] headers = {
                "Date",
                "Category",
                "Description",
                "Type",
                "Amount"
        };

        float[] widths = {
                70,
                85,
                190,
                60,
                65
        };

        drawTableHeader(
                context.content(),
                y,
                headers,
                widths
        );

        float rowY = y - TABLE_HEADER_HEIGHT;
        boolean alternate = false;

        for (PdfExportData.TransactionRow transaction : transactions) {

            if (rowY < 55) {
                context.startNewPage();

                rowY =
                        PAGE_SIZE.getHeight()
                                - PAGE_MARGIN
                                - TABLE_HEADER_HEIGHT;

                drawTableHeader(
                        context.content(),
                        rowY,
                        headers,
                        widths
                );

                rowY -= TABLE_HEADER_HEIGHT;
            }

            drawTableRowBackground(
                    context.content(),
                    rowY,
                    widths,
                    alternate
            );

            String amount = formatEuro(
                    transaction.amount()
            );

            if ("Ingreso".equals(transaction.type())) {
                amount = "+" + amount;
            } else {
                amount = "-" + amount;
            }

            String date =
                    transaction.date() == null
                            ? ""
                            : transaction.date()
                            .format(DATE_TIME_FORMATTER);

            String[] values = {
                    date,
                    safe(transaction.categoryName()),
                    safe(transaction.description()),
                    safe(transaction.type()),
                    amount
            };

            drawTableRow(
                    context.content(),
                    rowY,
                    values,
                    widths
            );

            rowY -= TABLE_ROW_HEIGHT;
            alternate = !alternate;
        }
    }

    private float drawSectionTitle(
            PDPageContentStream content,
            String title,
            float y
    ) throws IOException {

        drawText(
                content,
                title,
                FONT_BOLD,
                14,
                PAGE_MARGIN,
                y,
                TEXT_COLOR_R,
                TEXT_COLOR_G,
                TEXT_COLOR_B
        );

        setStrokingColor(content);

        content.setLineWidth(1.2f);

        content.moveTo(
                PAGE_MARGIN,
                y - SPACE_TITLE_TO_DIVIDER
        );

        content.lineTo(
                PAGE_SIZE.getWidth() - PAGE_MARGIN,
                y - SPACE_TITLE_TO_DIVIDER
        );

        content.stroke();

        return y - SPACE_TITLE_TO_DIVIDER;
    }

    private void drawEmptyMessage(
            PDPageContentStream content,
            String message,
            float y
    ) throws IOException {

        drawText(
                content,
                message,
                FONT_ITALIC,
                10,
                PAGE_MARGIN,
                y,
                MUTED_COLOR_R,
                MUTED_COLOR_G,
                MUTED_COLOR_B
        );
    }

    private void drawTableHeader(
            PDPageContentStream content,
            float y,
            String[] headers,
            float[] widths
    ) throws IOException {

        float x = PAGE_MARGIN;

        setNonStrokingColor(
                content,
                GREEN_R,
                GREEN_G,
                GREEN_B
        );

        content.addRect(
                x,
                y - TABLE_HEADER_HEIGHT,
                sum(widths),
                TABLE_HEADER_HEIGHT
        );

        content.fill();

        for (int i = 0; i < headers.length; i++) {

            drawText(
                    content,
                    headers[i].toUpperCase(Locale.ENGLISH),
                    FONT_BOLD,
                    7.5f,
                    x + 6,
                    y - (TABLE_HEADER_HEIGHT / 2f) - 2.5f,
                    CARD_R,
                    CARD_G,
                    CARD_B
            );

            x += widths[i];
        }
    }

    private void drawTableRowBackground(
            PDPageContentStream content,
            float y,
            float[] widths,
            boolean alternate
    ) throws IOException {

        if (!alternate) {
            return;
        }

        setNonStrokingColor(
                content,
                ALTERNATE_ROW_R,
                ALTERNATE_ROW_G,
                ALTERNATE_ROW_B
        );

        content.addRect(
                PAGE_MARGIN,
                y - TABLE_ROW_HEIGHT,
                sum(widths),
                TABLE_ROW_HEIGHT
        );

        content.fill();
    }

    private void drawTableRow(
            PDPageContentStream content,
            float y,
            String[] values,
            float[] widths
    ) throws IOException {

        float x = PAGE_MARGIN;

        for (int i = 0; i < values.length; i++) {

            String value = truncate(
                    values[i],
                    widths[i]
            );

            int textR = TEXT_COLOR_R;
            int textG = TEXT_COLOR_G;
            int textB = TEXT_COLOR_B;

            if (i == values.length - 1) {

                String originalValue = safe(values[i]);

                if (originalValue.startsWith("+")) {
                    textR = GREEN_R;
                    textG = GREEN_G;
                    textB = GREEN_B;
                } else if (originalValue.startsWith("-")) {
                    textR = RED_R;
                    textG = RED_G;
                    textB = RED_B;
                }
            }

            drawText(
                    content,
                    value,
                    FONT_REGULAR,
                    8,
                    x + 6,
                    y - (TABLE_ROW_HEIGHT / 2f) - 3f,
                    textR,
                    textG,
                    textB
            );

            x += widths[i];
        }

        setStrokingColor(content);

        content.setLineWidth(0.4f);

        content.moveTo(
                PAGE_MARGIN,
                y - TABLE_ROW_HEIGHT
        );

        content.lineTo(
                PAGE_MARGIN + sum(widths),
                y - TABLE_ROW_HEIGHT
        );

        content.stroke();
    }

    private void drawText(
            PDPageContentStream content,
            String text,
            PDType1Font font,
            float size,
            float x,
            float y,
            int r,
            int g,
            int b
    ) throws IOException {

        content.beginText();
        content.setFont(font, size);

        setNonStrokingColor(
                content,
                r,
                g,
                b
        );

        content.newLineAtOffset(x, y);
        content.showText(safe(text));
        content.endText();
    }

    private static void setNonStrokingColor(
            PDPageContentStream content,
            int r,
            int g,
            int b
    ) throws IOException {

        content.setNonStrokingColor(
                colorComponent(r),
                colorComponent(g),
                colorComponent(b)
        );
    }

    private static void setStrokingColor(
            PDPageContentStream content
    ) throws IOException {

        content.setStrokingColor(
                colorComponent(LINE_R),
                colorComponent(LINE_G),
                colorComponent(LINE_B)
        );
    }

    private static float colorComponent(int value) {
        return Math.clamp(value, 0, 255) / 255f;
    }

    private static String formatEuro(BigDecimal value) {
        if (value == null) {
            return "0€";
        }

        return String.format(
                Locale.ENGLISH,
                "%,.2f€",
                value
        );
    }

    private static String formatSignedEuro(BigDecimal value) {
        if (value == null) {
            return "0€";
        }

        String formatted = formatEuro(value);

        return value.compareTo(BigDecimal.ZERO) >= 0
                ? "+" + formatted
                : formatted;
    }

    private static String formatDecimal(
            BigDecimal value,
            int decimals
    ) {

        if (value == null) {
            return "0";
        }

        return value
                .setScale(
                        decimals,
                        java.math.RoundingMode.HALF_UP
                )
                .toPlainString();
    }

    private static String formatDecimal(double value) {
        return String.format(
                Locale.ENGLISH,
                "%.1f",
                value
        );
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static float sum(float[] values) {
        float result = 0;

        for (float value : values) {
            result += value;
        }

        return result;
    }

    private static String truncate(
            String value,
            float width
    ) {

        String safeValue = safe(value);

        float maxChars =
                Math.max(
                        4,
                        width / (7 * 0.55f)
                );

        int maxLength = (int) maxChars;

        if (safeValue.length() <= maxLength) {
            return safeValue;
        }

        return safeValue.substring(
                0,
                Math.max(1, maxLength - 3)
        ) + "...";
    }

    private static final class PdfPageContext {

        private final PDDocument document;
        private PDPageContentStream content;

        private PdfPageContext(
                PDDocument document,
                PDPageContentStream content
        ) {
            this.document = document;
            this.content = content;
        }

        private PDPageContentStream content() {
            return content;
        }

        private void startNewPage() throws IOException {
            content.close();

            PDPage page = new PDPage(PAGE_SIZE);
            document.addPage(page);

            content = new PDPageContentStream(
                    document,
                    page
            );

            drawBackground(content);
        }
    }
}