package com.puntomartinez.millete.dataexport.domain.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/** Reads current ISO instants and legacy wall times written in Europe/Madrid. */
public class LegacyCompatibleInstantDeserializer
        extends JsonDeserializer<Instant> {

    private static final ZoneId LEGACY_ZONE = ZoneId.of("Europe/Madrid");

    @Override
    public Instant deserialize(
            JsonParser parser,
            DeserializationContext context
    ) throws IOException {
        String value = parser.getValueAsString();
        try {
            return Instant.parse(value);
        } catch (RuntimeException ignored) {
            try {
                return OffsetDateTime.parse(value).toInstant();
            } catch (RuntimeException alsoIgnored) {
                return LocalDateTime.parse(value)
                        .atZone(LEGACY_ZONE)
                        .toInstant();
            }
        }
    }
}
