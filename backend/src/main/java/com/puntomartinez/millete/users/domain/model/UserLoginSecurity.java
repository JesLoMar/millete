package com.puntomartinez.millete.users.domain.model;

import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

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
            if (timeProvider.now().isAfter(this.blockedUntil)) {
                this.blockedUntil = null;
                this.modifiedAt = timeProvider.now();
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
        this.lastAttemptAt = timeProvider.now();
        this.modifiedAt = timeProvider.now();

        if (this.failedAttempts >= maxAttempts) {
            long duration = calculateLockDurationMinutes(
                    maxAttempts, baseLockDurationMinutes
            );
            this.blockedUntil = timeProvider.now()
                    .plus(duration, ChronoUnit.MINUTES);
        }
    }

    private long calculateLockDurationMinutes(
            int maxAttempts,
            long baseLockDurationMinutes
    ) {
        int extraAttempts = this.failedAttempts - maxAttempts;
        long duration = baseLockDurationMinutes
                << Math.min(extraAttempts, 6);
        return Math.min(duration, MAX_LOCK_DURATION_MINUTES);
    }

    public void resetAttempts(TimeProvider timeProvider) {
        if (this.failedAttempts > 0 || this.blockedUntil != null) {
            this.failedAttempts = 0;
            this.blockedUntil = null;
            this.lastAttemptAt = timeProvider.now();
            this.modifiedAt = timeProvider.now();
        }
    }
}