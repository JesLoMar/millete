package com.puntomartinez.millete.shared.domain.time;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TimeProvider - contrato del puerto de tiempo")
class TimeProviderTest {

    /**
     * Implementación mínima usada para verificar el comportamiento por defecto
     * del contrato (el método deprecated localDateTimeNow()).
     */
    private static TimeProvider providerAt(Instant instant, ZoneId zone) {
        return new TimeProvider() {
            @Override
            public Instant instantNow() {
                return instant;
            }

            @Override
            public LocalDate localDateNow() {
                return LocalDate.ofInstant(instant, zone);
            }

            @Override
            public ZoneId zone() {
                return zone;
            }
        };
    }

    @Nested
    @DisplayName("localDateTimeNow() derivado del contrato")
    class DefaultLocalDateTimeNow {

        @Test
        @DisplayName("deriva el date-time local a partir del instante y la zona de referencia")
        void derivaDateTimeDelInstanteYLaZona() {
            // 2026-09-25T22:00:00Z -> en Europe/Madrid son las 00:00 del día siguiente (CEST, UTC+2)
            Instant instant = Instant.parse("2026-09-25T22:00:00Z");
            ZoneId madrid = ZoneId.of("Europe/Madrid");

            LocalDateTime result = providerAt(instant, madrid).localDateTimeNow();

            assertThat(result).isEqualTo(LocalDateTime.parse("2026-09-26T00:00:00"));
        }

        @Test
        @DisplayName("con zona UTC el date-time local coincide con el instante")
        void conZonaUtcCoincideConElInstante() {
            Instant instant = Instant.parse("2026-09-25T16:37:21Z");

            LocalDateTime result = providerAt(instant, ZoneOffset.UTC).localDateTimeNow();

            assertThat(result).isEqualTo(LocalDateTime.parse("2026-09-25T16:37:21"));
        }
    }
}
