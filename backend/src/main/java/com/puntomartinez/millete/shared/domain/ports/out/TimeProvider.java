package com.puntomartinez.millete.shared.domain.ports.out;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public interface TimeProvider {

    Instant now();

    LocalDate localDateNow();

    @Deprecated(forRemoval = true)
    LocalDateTime nowLocal();

    LocalDate todayFor(ZoneId zone);

    ZoneId zone();

    Clock clock();
}