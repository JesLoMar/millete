package com.puntomartinez.millete.dataexport.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        LocalDateTime createdAt,

        @JsonProperty("modifiedAt")
        LocalDateTime modifiedAt,

        @JsonProperty("active")
        boolean active,

        @JsonProperty("lastExecutedDate")
        LocalDate lastExecutedDate
) {
}