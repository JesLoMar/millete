package com.puntomartinez.millete.dataexport.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

public record PlannedTransactionSnapshot(
        @JsonProperty("id")
        UUID id,

        @JsonProperty("userId")
        UUID userId,

        @JsonProperty("categoryId")
        UUID categoryId,

        @JsonProperty("amount")
        BigDecimal amount,

        @JsonProperty("type")
        String type,

        @JsonProperty("description")
        String description,

        @JsonProperty("frequencyType")
        String frequencyType,

        @JsonProperty("frequencyInterval")
        Integer frequencyInterval,

        @JsonProperty("startDate")
        LocalDate startDate,

        @JsonProperty("endDate")
        LocalDate endDate,

        @JsonProperty("createdAt")
        @JsonDeserialize(using = LegacyCompatibleInstantDeserializer.class)
        Instant createdAt,

        @JsonProperty("modifiedAt")
        @JsonDeserialize(using = LegacyCompatibleInstantDeserializer.class)
        Instant modifiedAt,

        @JsonProperty("active")
        boolean active,

        @JsonProperty("lastExecutedDate")
        LocalDate lastExecutedDate
) {
}
