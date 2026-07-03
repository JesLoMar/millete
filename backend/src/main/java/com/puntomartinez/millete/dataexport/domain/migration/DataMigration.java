package com.puntomartinez.millete.dataexport.domain.migration;

import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;

public interface DataMigration {


    ExportVersion fromVersion();


    ExportVersion toVersion();


    String description();


    UserDataSnapshot migrate(UserDataSnapshot snapshot);
}
