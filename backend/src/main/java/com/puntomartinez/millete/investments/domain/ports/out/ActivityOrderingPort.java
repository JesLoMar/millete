package com.puntomartinez.millete.investments.domain.ports.out;

import java.time.Instant;
import java.util.UUID;

public interface ActivityOrderingPort {

    long nextOrderingKey(
            UUID userId,
            Instant occurredAt
    );
}