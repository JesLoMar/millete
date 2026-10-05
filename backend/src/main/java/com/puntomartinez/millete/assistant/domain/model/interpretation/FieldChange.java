package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.util.Objects;

public record FieldChange<T>(
        boolean specified,
        T value
) {

    public FieldChange {
        if (!specified && value != null) {
            throw new IllegalArgumentException(
                    "An unspecified field change cannot have a value"
            );
        }
    }

    public static <T> FieldChange<T> unchanged() {
        return new FieldChange<>(false, null);
    }

    public static <T> FieldChange<T> set(T value) {
        return new FieldChange<>(
                true,
                Objects.requireNonNull(
                        value,
                        "value cannot be null when setting a field"
                )
        );
    }

    public static <T> FieldChange<T> clear() {
        return new FieldChange<>(true, null);
    }
}