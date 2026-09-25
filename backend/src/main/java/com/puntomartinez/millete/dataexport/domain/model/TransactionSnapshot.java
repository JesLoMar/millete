package com.puntomartinez.millete.dataexport.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionSnapshot(
        @JsonProperty("id")
        UUID id,

        @JsonProperty("userId")
        UUID userId,

        @JsonProperty("categoryId")
        UUID categoryId,

        @JsonProperty("amount")
        BigDecimal amount,

        @JsonProperty("date")
        LocalDate date,

        @JsonProperty("type")
        String type,

        @JsonProperty("description")
        String description,

        @JsonProperty("createdAt")
        Instant createdAt,

        @JsonProperty("modifiedAt")
        Instant modifiedAt,

        @JsonProperty("active")
        boolean active
) {
}