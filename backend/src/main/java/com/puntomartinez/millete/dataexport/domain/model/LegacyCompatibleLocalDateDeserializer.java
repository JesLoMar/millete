package com.puntomartinez.millete.dataexport.domain.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Reads both the legacy local date-time representation and the new date-only format. */
public class LegacyCompatibleLocalDateDeserializer
        extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(
            JsonParser parser,
            DeserializationContext context
    ) throws IOException {
        String value = parser.getValueAsString();
        try {
            return LocalDate.parse(value);
        } catch (RuntimeException ignored) {
            return LocalDateTime.parse(value).toLocalDate();
        }
    }
}
