package com.puntomartinez.millete.dataexport.domain.ports.out;

import com.puntomartinez.millete.dataexport.domain.model.ExportData;

public interface FileCsvExportPort {

    byte[] generateCsv(
            ExportData exportData,
            String entityType
    );
}