package com.puntomartinez.millete.shared.infrastructure.config.time;

import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SystemTimeProvider - adaptador de reloj del sistema")
class SystemTimeProviderTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-09-25T16:37:21Z");

    @Nested
    @DisplayName("Reloj fijo inyectable (determinismo en tests)")
    class RelojFijo {

        @Test
        @DisplayName("instantNow() devuelve exactamente el instante del reloj fijo")
        void instantNowDevuelveElInstanteFijo() {
            TimeProvider provider = new SystemTimeProvider(Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC));

            assertThat(provider.instantNow()).isEqualTo(FIXED_INSTANT);
        }

        @Test
        @DisplayName("varias llamadas consecutivas devuelven el mismo instante (consistencia)")
        void variasLlamadasDevuelvenElMismoInstante() {
            TimeProvider provider = new SystemTimeProvider(Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC));

            assertThat(provider.instantNow())
                    .isEqualTo(provider.instantNow())
                    .isEqualTo(provider.instantNow());
        }

        @Test
        @DisplayName("localDateNow() devuelve la fecha de calendario del instante en la zona del reloj")
        void localDateNowDevuelveLaFechaCorrecta() {
            TimeProvider provider = new SystemTimeProvider(Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC));

            assertThat(provider.localDateNow()).isEqualTo(LocalDate.of(2026, 9, 25));
        }

        @Test
        @DisplayName("localDateTimeNow() devuelve el date-time local en la zona del reloj")
        void localDateTimeNowDevuelveElDateTimeLocal() {
            TimeProvider provider = new SystemTimeProvider(Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC));

            assertThat(provider.localDateTimeNow()).isEqualTo(LocalDateTime.parse("2026-09-25T16:37:21"));
        }

        @Test
        @DisplayName("zone() expone la zona de referencia del reloj")
        void zoneExponeLaZonaDelReloj() {
            TimeProvider provider = new SystemTimeProvider(Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC));

            assertThat(provider.zone()).isEqualTo(ZoneOffset.UTC);
        }
    }

    @Nested
    @DisplayName("Comportamiento multi-zona")
    class MultiZona {

        @Test
        @DisplayName("el mismo instante produce fechas distintas según la zona (Auckland vs. Samoa)")
        void mismoInstanteFechasDistintasSegunZona() {
            // 2026-09-24T23:00Z: en Pacific/Auckland (NZST, UTC+12) ya es el día 25 a las 11:00;
            // en Pacific/Pago_Pago (UTC-11) aún es el día 24 a las 12:00.
            Instant boundary = Instant.parse("2026-09-24T23:00:00Z");

            TimeProvider auckland = new SystemTimeProvider(Clock.fixed(boundary, ZoneId.of("Pacific/Auckland")));
            TimeProvider samoa = new SystemTimeProvider(Clock.fixed(boundary, ZoneId.of("Pacific/Pago_Pago")));

            assertThat(auckland.localDateNow()).isEqualTo(LocalDate.of(2026, 9, 25));
            assertThat(samoa.localDateNow()).isEqualTo(LocalDate.of(2026, 9, 24));
            assertThat(auckland.instantNow()).isEqualTo(samoa.instantNow());
        }
    }

    @Nested
    @DisplayName("Constructor por defecto (reloj del sistema en UTC)")
    class RelojDelSistema {

        @Test
        @DisplayName("usa UTC como zona de referencia")
        void usaUtcComoZonaDeReferencia() {
            TimeProvider provider = new SystemTimeProvider();

            assertThat(provider.zone()).isEqualTo(ZoneOffset.UTC);
        }

        @Test
        @DisplayName("instantNow() es coherente con el reloj del sistema")
        void instantNowEsCoherenteConElRelojDelSistema() {
            TimeProvider provider = new SystemTimeProvider();
            Instant before = Instant.now().minusSeconds(5);

            Instant result = provider.instantNow();

            assertThat(result).isAfterOrEqualTo(before).isBeforeOrEqualTo(Instant.now().plusSeconds(5));
        }
    }
}
