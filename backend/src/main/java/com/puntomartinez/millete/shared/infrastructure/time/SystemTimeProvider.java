package com.puntomartinez.millete.shared.infrastructure.time;

import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class SystemTimeProvider implements TimeProvider {

    private final Clock clock;

    public SystemTimeProvider(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Instant now() {
        return clock.instant();
    }

    @Override
    public LocalDate localDateNow() {
        return LocalDate.ofInstant(clock.instant(), clock.getZone());
    }

    @Override
    @Deprecated(forRemoval = true)
    public LocalDateTime nowLocal() {
        return LocalDateTime.now(clock);
    }

    @Override
    public LocalDate todayFor(ZoneId zone) {
        return clock.instant().atZone(zone).toLocalDate();
    }

    @Override
    public ZoneId zone() {
        return clock.getZone();
    }

    @Override
    public Clock clock() {
        return clock;
    }
}