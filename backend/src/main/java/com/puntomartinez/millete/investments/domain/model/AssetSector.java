package com.puntomartinez.millete.investments.domain.model;

import java.util.UUID;

public record AssetSector(UUID id, String code, String displayName, boolean active) {
    public AssetSector {
        if (id == null || code == null || code.isBlank() || displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Sector id, code and displayName are required");
        }
        code = code.trim().toUpperCase();
        displayName = displayName.trim();
    }
}
