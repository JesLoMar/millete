package com.puntomartinez.millete.shared.infrastructure.config.time;

import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Adaptador de infraestructura por defecto de {@link TimeProvider}.
 *
 * <p>Usa un {@link Clock} interno (por defecto, el reloj del sistema en UTC).
 * La elección de UTC como zona de referencia del servidor mantiene la
 * consistencia con las columnas TIMESTAMPTZ de la base de datos y evita
 * ambigüedades causadas por la zona del host.</p>
 */
@Component
public class SystemTimeProvider implements TimeProvider {

    private final Clock clock;

    public SystemTimeProvider() {
        this(Clock.systemUTC());
    }

    SystemTimeProvider(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Instant instantNow() {
        return clock.instant();
    }

    @Override
    public LocalDate localDateNow() {
        return LocalDate.ofInstant(clock.instant(), clock.getZone());
    }

    @Override
    @Deprecated(forRemoval = true)
    public LocalDateTime localDateTimeNow() {
        return LocalDateTime.ofInstant(clock.instant(), clock.getZone());
    }

    @Override
    public ZoneId zone() {
        return clock.getZone();
    }
}
