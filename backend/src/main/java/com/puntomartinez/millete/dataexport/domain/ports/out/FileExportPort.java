package com.puntomartinez.millete.dataexport.domain.ports.out;

import com.puntomartinez.millete.dataexport.domain.model.ExportData;
import com.puntomartinez.millete.dataexport.domain.model.PdfExportData;

public interface FileExportPort {
    byte[] generateZip(ExportData exportData);
    byte[] generateCsv(ExportData exportData, String entityType);
    byte[] generatePdf(PdfExportData pdfExportData);
}