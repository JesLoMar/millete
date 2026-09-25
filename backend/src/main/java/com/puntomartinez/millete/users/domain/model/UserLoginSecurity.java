package com.puntomartinez.millete.users.domain.model;

import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Estado de seguridad de login por cuenta.
 *
 * <p>Tras la Fase 1 de la normalización temporal, todos sus momentos se
 * modelan con {@link Instant} (la tabla {@code user_login_security} ya usaba
 * TIMESTAMPTZ desde V3). El tiempo actual se obtiene exclusivamente a través
 * del puerto {@link TimeProvider}, lo que hace deterministas las comparaciones
 * de bloqueo en tests multi-zona.</p>
 */
@Getter
@Setter
public class UserLoginSecurity {

    private static final long MAX_LOCK_DURATION_MINUTES = 1440;

    private UUID userId;
    private int failedAttempts;
    private Instant blockedUntil;
    private Instant lastAttemptAt;
    private Instant createdAt;
    private Instant modifiedAt;

    public UserLoginSecurity() {}

    public boolean isBlocked(TimeProvider timeProvider) {
        if (this.blockedUntil != null) {
            if (timeProvider.instantNow().isAfter(this.blockedUntil)) {
                this.blockedUntil = null;
                this.modifiedAt = timeProvider.instantNow();
                return false;
            }
            return true;
        }
        return false;
    }

    public void registerFailedAttempt(
            TimeProvider timeProvider,
            int maxAttempts,
            long baseLockDurationMinutes
    ) {
        this.failedAttempts++;
        this.lastAttemptAt = timeProvider.instantNow();
        this.modifiedAt = timeProvider.instantNow();
        if (this.failedAttempts >= maxAttempts) {
            this.blockedUntil = timeProvider.instantNow()
                    .plus(calculateLockDurationMinutes(maxAttempts, baseLockDurationMinutes), ChronoUnit.MINUTES);
        }
    }

    private long calculateLockDurationMinutes(int maxAttempts, long baseLockDurationMinutes) {
        int extraAttempts = this.failedAttempts - maxAttempts;
        long duration = baseLockDurationMinutes << Math.min(extraAttempts, 6);
        return Math.min(duration, MAX_LOCK_DURATION_MINUTES);
    }

    public void resetAttempts(TimeProvider timeProvider) {
        if (this.failedAttempts > 0 || this.blockedUntil != null) {
            this.failedAttempts = 0;
            this.blockedUntil = null;
            this.lastAttemptAt = timeProvider.instantNow();
            this.modifiedAt = timeProvider.instantNow();
        }
    }
}