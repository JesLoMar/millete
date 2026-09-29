package com.puntomartinez.millete.investments.domain.ports.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ActivityIdempotencyRepository {

    Optional<IdempotencyEntry> findByUserIdAndKey(
            UUID userId,
            String key
    );

    void save(IdempotencyEntry entry);

    record IdempotencyEntry(
            UUID userId,
            String key,
            String requestFingerprint,
            UUID activityId,
            Instant createdAt
    ) {
    }
}