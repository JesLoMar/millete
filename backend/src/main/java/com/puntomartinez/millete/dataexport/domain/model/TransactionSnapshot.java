package com.puntomartinez.millete.dataexport.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        LocalDateTime date,

        @JsonProperty("type")
        String type,

        @JsonProperty("description")
        String description,

        @JsonProperty("createdAt")
        LocalDateTime createdAt,

        @JsonProperty("modifiedAt")
        LocalDateTime modifiedAt,

        @JsonProperty("active")
        boolean active
) {
}