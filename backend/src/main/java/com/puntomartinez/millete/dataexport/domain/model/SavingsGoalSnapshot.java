package com.puntomartinez.millete.dataexport.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

public record SavingsGoalSnapshot(
        @JsonProperty("id")
        UUID id,

        @JsonProperty("userId")
        UUID userId,

        @JsonProperty("name")
        String name,

        @JsonProperty("targetAmount")
        BigDecimal targetAmount,

        @JsonProperty("currentAmount")
        BigDecimal currentAmount,

        @JsonProperty("deadline")
        LocalDate deadline,

        @JsonProperty("priority")
        String priority,

        @JsonProperty("link")
        String link,

        @JsonProperty("createdAt")
        @JsonDeserialize(using = LegacyCompatibleInstantDeserializer.class)
        Instant createdAt,

        @JsonProperty("modifiedAt")
        @JsonDeserialize(using = LegacyCompatibleInstantDeserializer.class)
        Instant modifiedAt,

        @JsonProperty("active")
        boolean active
) {}
