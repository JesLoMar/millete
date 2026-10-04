package com.puntomartinez.millete.assistant.domain.model;

public record Confidence(
        double probability,
        double margin,
        boolean abstain
) {

    public Confidence {
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("Probability must be between 0 and 1");
        }

        if (margin < 0.0 || margin > 1.0) {
            throw new IllegalArgumentException("Margin must be between 0 and 1");
        }
    }
}