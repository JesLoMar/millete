package com.puntomartinez.millete.dataexport.infrastructure.out.fileexport;

import com.puntomartinez.millete.dataexport.domain.model.PdfExportData;
import com.puntomartinez.millete.dataexport.domain.model.ExportData;
import com.puntomartinez.millete.dataexport.domain.ports.out.FileExportPort;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component("zipFileExportAdapter")
public class ZipFileExportAdapter implements FileExportPort {


    private static final String FORMULA_PREFIXES = "=+-@\t\r";

    @Override
    public byte[] generateZip(ExportData data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {

            writeCsvToZip(zos, "categories.csv",
                    new String[]{"name", "budget_limit"},
                    data.categories(),
                    (csv, row) -> csv.printRecord(sanitizeCsvField(row.name()), row.budgetLimit()));

            writeCsvToZip(zos, "transactions.csv",
                    new String[]{"category_name", "amount", "date", "type", "description"},
                    data.transactions(),
                    (csv, row) -> csv.printRecord(
                            sanitizeCsvField(row.categoryName()),
                            row.amount(),
                            row.date(),
                            sanitizeCsvField(row.type()),
                            sanitizeCsvField(row.description())));

            writeCsvToZip(zos, "planned_transactions.csv",
                    new String[]{"category_name", "amount", "type", "description", "frequency_type", "frequency_interval", "start_date", "end_date", "last_executed_date"},
                    data.plannedTransactions(),
                    (csv, row) -> csv.printRecord(
                            sanitizeCsvField(row.categoryName()),
                            row.amount(),
                            sanitizeCsvField(row.type()),
                            sanitizeCsvField(row.description()),
                            sanitizeCsvField(row.frequencyType()),
                            row.frequencyInterval(),
                            row.startDate(),
                            row.endDate(),
                            row.lastExecutedDate()));

            writeCsvToZip(zos, "investments.csv",
                    new String[]{"asset_name", "ticker", "quantity", "purchase_price", "current_price", "type", "purchase_date"},
                    data.investments(),
                    (csv, row) -> csv.printRecord(
                            sanitizeCsvField(row.assetName()),
                            sanitizeCsvField(row.ticker()),
                            row.quantity(),
                            row.purchasePrice(),
                            row.currentPrice(),
                            sanitizeCsvField(row.type()),
                            row.purchaseDate()));

            writeCsvToZip(zos, "savings_goals.csv",
                    new String[]{"name", "target_amount", "current_amount", "progress", "deadline", "priority", "status", "link"},
                    data.savingsGoals(),
                    (csv, row) -> csv.printRecord(
                            sanitizeCsvField(row.name()),
                            row.targetAmount(),
                            row.currentAmount(),
                            row.progress(),
                            row.deadline(),
                            sanitizeCsvField(row.priority()),
                            sanitizeCsvField(row.status()),
                            sanitizeCsvField(row.link())));

            zos.finish();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generando ZIP de exportación", e);
        }
    }

    @Override
    public byte[] generatePdf(PdfExportData pdfExportData) {
        return new byte[0];
    }

    @FunctionalInterface
    private interface RowWriter<T> {
        void write(CSVPrinter csv, T row) throws IOException;
    }

    private <T> void writeCsvToZip(ZipOutputStream zos, String fileName, String[] headers,
                                   java.util.List<T> rows, RowWriter<T> writer) throws IOException {
        zos.putNextEntry(new ZipEntry(fileName));

        ByteArrayOutputStream csvBaos = new ByteArrayOutputStream();
        try (OutputStreamWriter osw = new OutputStreamWriter(csvBaos, StandardCharsets.UTF_8);
             CSVPrinter csv = new CSVPrinter(osw, CSVFormat.DEFAULT.withHeader(headers))) {

            if (rows != null) {
                for (T row : rows) {
                    writer.write(csv, row);
                }
            }
            csv.flush();
        }

        zos.write(csvBaos.toByteArray());
        zos.closeEntry();
    }

    @Override
    public byte[] generateCsv(ExportData data, String entityType) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT)) {

            switch (entityType.toLowerCase()) {
                case "categories" -> {
                    csv.printRecord("name", "budget_limit");
                    if (data.categories() != null) {
                        for (var row : data.categories()) {
                            csv.printRecord(sanitizeCsvField(row.name()), row.budgetLimit());
                        }
                    }
                }
                case "transactions" -> {
                    csv.printRecord("category_name", "amount", "date", "type", "description");
                    if (data.transactions() != null) {
                        for (var row : data.transactions()) {
                            csv.printRecord(
                                    sanitizeCsvField(row.categoryName()),
                                    row.amount(),
                                    row.date(),
                                    sanitizeCsvField(row.type()),
                                    sanitizeCsvField(row.description()));
                        }
                    }
                }
                case "planned_transactions" -> {
                    csv.printRecord("category_name", "amount", "type", "description", "frequency_type", "frequency_interval", "start_date", "end_date", "last_executed_date");
                    if (data.plannedTransactions() != null) {
                        for (var row : data.plannedTransactions()) {
                            csv.printRecord(
                                    sanitizeCsvField(row.categoryName()),
                                    row.amount(),
                                    sanitizeCsvField(row.type()),
                                    sanitizeCsvField(row.description()),
                                    sanitizeCsvField(row.frequencyType()),
                                    row.frequencyInterval(),
                                    row.startDate(),
                                    row.endDate(),
                                    row.lastExecutedDate());
                        }
                    }
                }
                case "investments" -> {
                    csv.printRecord("asset_name", "ticker", "quantity", "purchase_price", "current_price", "type", "purchase_date");
                    if (data.investments() != null) {
                        for (var row : data.investments()) {
                            csv.printRecord(
                                    sanitizeCsvField(row.assetName()),
                                    sanitizeCsvField(row.ticker()),
                                    row.quantity(),
                                    row.purchasePrice(),
                                    row.currentPrice(),
                                    sanitizeCsvField(row.type()),
                                    row.purchaseDate());
                        }
                    }
                }

                case "savings_goals", "savingsgoals" -> {
                    csv.printRecord("name", "target_amount", "current_amount", "progress", "deadline", "priority", "status", "link");
                    if (data.savingsGoals() != null) {
                        for (var row : data.savingsGoals()) {
                            csv.printRecord(
                                    sanitizeCsvField(row.name()),
                                    row.targetAmount(),
                                    row.currentAmount(),
                                    row.progress(),
                                    row.deadline(),
                                    sanitizeCsvField(row.priority()),
                                    sanitizeCsvField(row.status()),
                                    sanitizeCsvField(row.link()));
                        }
                    }
                }

                default -> throw new IllegalArgumentException("Tipo de entidad no válido: " + entityType);
            }

            csv.flush();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generando CSV para " + entityType, e);
        }
    }


    private String sanitizeCsvField(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        char first = value.charAt(0);
        if (FORMULA_PREFIXES.indexOf(first) >= 0) {
            return "'" + value;
        }
        return value;
    }
}
