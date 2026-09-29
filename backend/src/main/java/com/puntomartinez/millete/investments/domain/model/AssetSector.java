package com.puntomartinez.millete.investments.domain.model;

public record AssetSector(
        String code,
        String displayName,
        boolean custom
) {

    public AssetSector {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del sector es obligatorio"
            );
        }

        displayName = displayName.trim();

        if (custom) {
            if (code != null && !code.isBlank()) {
                throw new IllegalArgumentException(
                        "Un sector personalizado no puede tener código de catálogo"
                );
            }

            code = null;
        } else {
            if (code == null || code.isBlank()) {
                throw new IllegalArgumentException(
                        "Un sector del catálogo requiere código"
                );
            }

            code = code.trim().toUpperCase();
        }
    }

    public static AssetSector common(
            String code,
            String displayName
    ) {
        return new AssetSector(
                code,
                displayName,
                false
        );
    }

    public static AssetSector custom(
            String displayName
    ) {
        return new AssetSector(
                null,
                displayName,
                true
        );
    }
}