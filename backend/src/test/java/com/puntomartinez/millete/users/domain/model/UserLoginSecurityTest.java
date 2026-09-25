package com.puntomartinez.millete.users.domain.model;

import com.puntomartinez.millete.shared.domain.time.FixedTimeProvider;
import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserLoginSecurity model")
class UserLoginSecurityTest {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BASE_LOCK_MINUTES = 15;

    private static final Instant NOW = Instant.parse("2026-09-26T10:00:00Z");

    private TimeProvider timeProvider;
    private UserLoginSecurity security;

    @BeforeEach
    void setUp() {
        timeProvider = new FixedTimeProvider(NOW);
        security = new UserLoginSecurity();
        security.setUserId(UUID.randomUUID());
        security.setFailedAttempts(0);
        security.setCreatedAt(NOW);
        security.setModifiedAt(NOW);
    }

    @Nested
    @DisplayName("isBlocked")
    class IsBlocked {

        @Test
        @DisplayName("Should return false when no block exists")
        void shouldReturnFalseWhenNoBlock() {
            security.setBlockedUntil(null);

            assertThat(security.isBlocked(timeProvider)).isFalse();
        }

        @Test
        @DisplayName("Should return true when block is active")
        void shouldReturnTrueWhenBlockIsActive() {
            security.setBlockedUntil(NOW.plus(10, ChronoUnit.MINUTES));

            assertThat(security.isBlocked(timeProvider)).isTrue();
        }

        @Test
        @DisplayName("Should return false and clear block when expired")
        void shouldReturnFalseAndClearWhenExpired() {
            security.setBlockedUntil(NOW.minus(5, ChronoUnit.MINUTES));

            boolean result = security.isBlocked(timeProvider);

            assertThat(result).isFalse();
            assertThat(security.getBlockedUntil()).isNull();
            assertThat(security.getModifiedAt()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("registerFailedAttempt")
    class RegisterFailedAttempt {

        @Test
        @DisplayName("Should increment attempts without blocking below threshold")
        void shouldIncrementWithoutBlockingBelowThreshold() {
            security.setFailedAttempts(3);

            security.registerFailedAttempt(timeProvider, MAX_ATTEMPTS, BASE_LOCK_MINUTES);

            assertThat(security.getFailedAttempts()).isEqualTo(4);
            assertThat(security.getBlockedUntil()).isNull();
            assertThat(security.getLastAttemptAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("Should block at max attempts with base duration")
        void shouldBlockAtMaxAttempts() {
            security.setFailedAttempts(4);

            security.registerFailedAttempt(timeProvider, MAX_ATTEMPTS, BASE_LOCK_MINUTES);

            assertThat(security.getFailedAttempts()).isEqualTo(5);
            assertThat(security.getBlockedUntil()).isEqualTo(NOW.plus(15, ChronoUnit.MINUTES));
        }

        @Test
        @DisplayName("Should escalate lock duration on subsequent failures")
        void shouldEscalateLockDuration() {
            security.setFailedAttempts(5);

            security.registerFailedAttempt(timeProvider, MAX_ATTEMPTS, BASE_LOCK_MINUTES);

            assertThat(security.getFailedAttempts()).isEqualTo(6);
            assertThat(security.getBlockedUntil()).isEqualTo(NOW.plus(30, ChronoUnit.MINUTES));
        }

        @Test
        @DisplayName("Should cap lock duration at 1440 minutes")
        void shouldCapLockDurationAtMax() {
            security.setFailedAttempts(15);

            security.registerFailedAttempt(timeProvider, MAX_ATTEMPTS, BASE_LOCK_MINUTES);

            assertThat(security.getBlockedUntil())
                    .isBeforeOrEqualTo(NOW.plus(1440, ChronoUnit.MINUTES));
        }
    }

    @Nested
    @DisplayName("resetAttempts")
    class ResetAttempts {

        @Test
        @DisplayName("Should reset attempts and block")
        void shouldResetAttemptsAndBlock() {
            security.setFailedAttempts(3);
            security.setBlockedUntil(NOW.plus(10, ChronoUnit.MINUTES));

            security.resetAttempts(timeProvider);

            assertThat(security.getFailedAttempts()).isZero();
            assertThat(security.getBlockedUntil()).isNull();
            assertThat(security.getLastAttemptAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("Should not modify when already clean")
        void shouldNotModifyWhenAlreadyClean() {
            security.setFailedAttempts(0);
            security.setBlockedUntil(null);
            Instant beforeModifiedAt = security.getModifiedAt();

            security.resetAttempts(timeProvider);

            assertThat(security.getFailedAttempts()).isZero();
            assertThat(security.getBlockedUntil()).isNull();
            assertThat(security.getModifiedAt()).isEqualTo(beforeModifiedAt);
        }
    }
}
