package com.puntomartinez.millete.dataexport.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.Instant;
import java.util.UUID;

public record UserPreferencesSnapshot(
        @JsonProperty("id")
        UUID id,

        @JsonProperty("userId")
        UUID userId,

        @JsonProperty("preferencesJson")
        String preferencesJson,

        @JsonProperty("createdAt")
        @JsonDeserialize(using = LegacyCompatibleInstantDeserializer.class)
        Instant createdAt,

        @JsonProperty("modifiedAt")
        @JsonDeserialize(using = LegacyCompatibleInstantDeserializer.class)
        Instant modifiedAt
) {
}
