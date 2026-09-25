package com.puntomartinez.millete.shared.domain.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Implementación de {@link TimeProvider} con un instante fijo, pensada para
 * tests deterministas (comparaciones de bloqueo, expiración de invitaciones,
 * ejecuciones programadas multi-zona, etc.).
 */
public class FixedTimeProvider implements TimeProvider {

    private final Instant instant;
    private final ZoneId zone;

    public FixedTimeProvider(Instant instant) {
        this(instant, ZoneId.of("UTC"));
    }

    public FixedTimeProvider(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    @Override
    public Instant instantNow() {
        return instant;
    }

    @Override
    public LocalDate localDateNow() {
        return instant.atZone(zone).toLocalDate();
    }

    @Override
    public ZoneId zone() {
        return zone;
    }
}
