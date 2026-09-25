package com.puntomartinez.millete.dataexport.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

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
        @JsonDeserialize(using = LegacyCompatibleLocalDateDeserializer.class)
        LocalDate date,

        @JsonProperty("type")
        String type,

        @JsonProperty("description")
        String description,

        @JsonProperty("createdAt")
        @JsonDeserialize(using = LegacyCompatibleInstantDeserializer.class)
        Instant createdAt,

        @JsonProperty("modifiedAt")
        @JsonDeserialize(using = LegacyCompatibleInstantDeserializer.class)
        Instant modifiedAt,

        @JsonProperty("active")
        boolean active
) {
}
