package com.puntomartinez.millete.dataexport.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDataSnapshot(
        @JsonProperty("metadata")
        SnapshotMetadata metadata,

        @JsonProperty("categories")
        List<CategorySnapshot> categories,

        @JsonProperty("transactions")
        List<TransactionSnapshot> transactions,

        @JsonProperty("plannedTransactions")
        List<PlannedTransactionSnapshot> plannedTransactions,

        @JsonProperty("investments")
        List<InvestmentSnapshot> investments,

        @JsonProperty("savingsGoals")
        List<SavingsGoalSnapshot> savingsGoals,

        @JsonProperty("userPreferences")
        UserPreferencesSnapshot userPreferences
) {

    public UserDataSnapshot {
        if (metadata == null) {
            throw new IllegalArgumentException(
                    "El metadata del snapshot es obligatorio"
            );
        }
    }

    public record SnapshotMetadata(
            @JsonProperty("version")
            String version,

            @JsonProperty("exportDate")
            LocalDateTime exportDate,

            @JsonProperty("appVersion")
            String appVersion
    ) {}
}