package com.puntomartinez.millete.dataexport.domain.ports.out;

import com.puntomartinez.millete.dataexport.domain.model.ExportData;

public interface FileZipExportPort {

    byte[] generateZip(ExportData exportData);
}