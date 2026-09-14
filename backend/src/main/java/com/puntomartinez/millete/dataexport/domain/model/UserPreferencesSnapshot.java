package com.puntomartinez.millete.dataexport.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserPreferencesSnapshot(
        @JsonProperty("id")
        UUID id,

        @JsonProperty("userId")
        UUID userId,

        @JsonProperty("preferencesJson")
        String preferencesJson,

        @JsonProperty("createdAt")
        LocalDateTime createdAt,

        @JsonProperty("modifiedAt")
        LocalDateTime modifiedAt
) {
}