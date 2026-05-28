package com.puntomartinez.millete.dataexport.domain.migration;

import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;

public interface DataMigration {

    // Versión de origen que acepta esta migración
    ExportVersion fromVersion();

    // Versión de destino que produce esta migración
    ExportVersion toVersion();

    // Descripción del cambio que realiza
    String description();

    // Ejecuta la transformación de los datos
    UserDataSnapshot migrate(UserDataSnapshot snapshot);
}