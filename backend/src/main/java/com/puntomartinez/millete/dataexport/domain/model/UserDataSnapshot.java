package com.puntomartinez.millete.dataexport.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.transactions.domain.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDataSnapshot(
        @JsonProperty("metadata")
        SnapshotMetadata metadata,

        @JsonProperty("categories")
        List<Category> categories,

        @JsonProperty("transactions")
        List<Transaction> transactions,

        @JsonProperty("plannedTransactions")
        List<PlannedTransaction> plannedTransactions,

        @JsonProperty("investments")
        List<Investment> investments
) {
    public UserDataSnapshot {
        if (metadata == null) {
            throw new IllegalArgumentException("El metadata del snapshot es obligatorio");
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