package com.puntomartinez.millete.assistant.domain.model.interpretation;

public record UnresolvedEntity(
        EntityType type,
        String reference,
        ResolutionStatus status
) {

    public enum EntityType {
        CATEGORY,
        TRANSACTION,
        RECURRING_TRANSACTION,
        SAVINGS_GOAL
    }

    public enum ResolutionStatus {
        NOT_FOUND,
        AMBIGUOUS
    }
}