package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.util.Objects;

public record CategoryResolution(
        CategoryResolutionStatus status,
        String reference,
        CategoryCandidate category,
        double probability,
        double margin
) {

    public CategoryResolution {
        Objects.requireNonNull(
                status,
                "status cannot be null"
        );

        Objects.requireNonNull(
                reference,
                "reference cannot be null"
        );

        if (reference.isBlank()) {
            throw new IllegalArgumentException(
                    "reference cannot be blank"
            );
        }

        if (probability < 0.0
                || probability > 1.0) {

            throw new IllegalArgumentException(
                    "probability must be between 0 and 1"
            );
        }

        if (margin < 0.0
                || margin > 1.0) {

            throw new IllegalArgumentException(
                    "margin must be between 0 and 1"
            );
        }

        if (status == CategoryResolutionStatus.FOUND
                && category == null) {

            throw new IllegalArgumentException(
                    "FOUND resolution requires a category"
            );
        }

        if (status != CategoryResolutionStatus.FOUND
                && category != null) {

            throw new IllegalArgumentException(
                    "Only FOUND resolution can contain a category"
            );
        }
    }

    public static CategoryResolution found(
            String reference,
            CategoryCandidate category,
            double probability,
            double margin
    ) {
        return new CategoryResolution(
                CategoryResolutionStatus.FOUND,
                reference,
                Objects.requireNonNull(
                        category,
                        "category cannot be null"
                ),
                probability,
                margin
        );
    }

    public static CategoryResolution notFound(
            String reference,
            double probability,
            double margin
    ) {
        return new CategoryResolution(
                CategoryResolutionStatus.NOT_FOUND,
                reference,
                null,
                probability,
                margin
        );
    }

    public static CategoryResolution ambiguous(
            String reference,
            double probability,
            double margin
    ) {
        return new CategoryResolution(
                CategoryResolutionStatus.AMBIGUOUS,
                reference,
                null,
                probability,
                margin
        );
    }
}