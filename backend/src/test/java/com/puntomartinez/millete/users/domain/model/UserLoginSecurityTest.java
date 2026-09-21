package com.puntomartinez.millete.users.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserLoginSecurity model")
class UserLoginSecurityTest {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BASE_LOCK_MINUTES = 15;

    private UserLoginSecurity security;

    @BeforeEach
    void setUp() {
        security = new UserLoginSecurity();
        security.setUserId(UUID.randomUUID());
        security.setFailedAttempts(0);
        security.setCreatedAt(LocalDateTime.now());
        security.setModifiedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("isBlocked")
    class IsBlocked {

        @Test
        @DisplayName("Should return false when no block exists")
        void shouldReturnFalseWhenNoBlock() {
            security.setBlockedUntil(null);

            assertThat(security.isBlocked()).isFalse();
        }

        @Test
        @DisplayName("Should return true when block is active")
        void shouldReturnTrueWhenBlockIsActive() {
            security.setBlockedUntil(LocalDateTime.now().plusMinutes(10));

            assertThat(security.isBlocked()).isTrue();
        }

        @Test
        @DisplayName("Should return false and clear block when expired")
        void shouldReturnFalseAndClearWhenExpired() {
            security.setBlockedUntil(LocalDateTime.now().minusMinutes(5));

            boolean result = security.isBlocked();

            assertThat(result).isFalse();
            assertThat(security.getBlockedUntil()).isNull();
            assertThat(security.getModifiedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("registerFailedAttempt")
    class RegisterFailedAttempt {

        @Test
        @DisplayName("Should increment attempts without blocking below threshold")
        void shouldIncrementWithoutBlockingBelowThreshold() {
            security.setFailedAttempts(3);

            security.registerFailedAttempt(MAX_ATTEMPTS, BASE_LOCK_MINUTES);

            assertThat(security.getFailedAttempts()).isEqualTo(4);
            assertThat(security.getBlockedUntil()).isNull();
            assertThat(security.getLastAttemptAt()).isNotNull();
        }

        @Test
        @DisplayName("Should block at max attempts with base duration")
        void shouldBlockAtMaxAttempts() {
            security.setFailedAttempts(4);

            security.registerFailedAttempt(MAX_ATTEMPTS, BASE_LOCK_MINUTES);

            assertThat(security.getFailedAttempts()).isEqualTo(5);
            assertThat(security.getBlockedUntil())
                    .isAfter(LocalDateTime.now().plusMinutes(14))
                    .isBefore(LocalDateTime.now().plusMinutes(16));
        }

        @Test
        @DisplayName("Should escalate lock duration on subsequent failures")
        void shouldEscalateLockDuration() {
            security.setFailedAttempts(5);

            security.registerFailedAttempt(MAX_ATTEMPTS, BASE_LOCK_MINUTES);

            assertThat(security.getFailedAttempts()).isEqualTo(6);
            assertThat(security.getBlockedUntil())
                    .isAfter(LocalDateTime.now().plusMinutes(29))
                    .isBefore(LocalDateTime.now().plusMinutes(31));
        }

        @Test
        @DisplayName("Should cap lock duration at 1440 minutes")
        void shouldCapLockDurationAtMax() {
            security.setFailedAttempts(15);

            security.registerFailedAttempt(MAX_ATTEMPTS, BASE_LOCK_MINUTES);

            assertThat(security.getBlockedUntil())
                    .isBefore(LocalDateTime.now().plusMinutes(1441));
        }
    }

    @Nested
    @DisplayName("resetAttempts")
    class ResetAttempts {

        @Test
        @DisplayName("Should reset attempts and block")
        void shouldResetAttemptsAndBlock() {
            security.setFailedAttempts(3);
            security.setBlockedUntil(LocalDateTime.now().plusMinutes(10));

            security.resetAttempts();

            assertThat(security.getFailedAttempts()).isZero();
            assertThat(security.getBlockedUntil()).isNull();
            assertThat(security.getLastAttemptAt()).isNotNull();
        }

        @Test
        @DisplayName("Should not modify when already clean")
        void shouldNotModifyWhenAlreadyClean() {
            security.setFailedAttempts(0);
            security.setBlockedUntil(null);
            LocalDateTime beforeModifiedAt = security.getModifiedAt();

            security.resetAttempts();

            assertThat(security.getFailedAttempts()).isZero();
            assertThat(security.getBlockedUntil()).isNull();
            assertThat(security.getModifiedAt()).isEqualTo(beforeModifiedAt);
        }
    }
}