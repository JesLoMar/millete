package com.puntomartinez.millete.dataexport.domain.model;

import java.util.Map;
import java.util.UUID;

public record CategoryImportResult(
        Map<UUID, UUID> categoryIdMap,
        int importedCount
) {
}