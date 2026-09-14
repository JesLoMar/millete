package com.puntomartinez.millete.dataexport.domain.ports.out;

import com.puntomartinez.millete.dataexport.domain.model.PdfExportData;

public interface FilePdfExportPort {

    byte[] generatePdf(PdfExportData pdfExportData);
}