package com.puntomartinez.millete.investments.domain.model;

import java.util.UUID;

public record AssetReference(
        AssetReferenceKind kind,
        UUID id
) {

    public AssetReference {
        if (kind == null) {
            throw new IllegalArgumentException(
                    "El tipo de referencia del Asset es obligatorio"
            );
        }

        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del Asset es obligatorio"
            );
        }
    }

    public static AssetReference shared(UUID id) {
        return new AssetReference(
                AssetReferenceKind.SHARED,
                id
        );
    }

    public static AssetReference user(UUID id) {
        return new AssetReference(
                AssetReferenceKind.USER,
                id
        );
    }
}