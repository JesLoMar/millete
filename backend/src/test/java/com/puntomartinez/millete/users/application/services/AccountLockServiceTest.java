package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import com.puntomartinez.millete.users.domain.exception.AccountLockedException;
import com.puntomartinez.millete.users.domain.model.UserLoginSecurity;
import com.puntomartinez.millete.users.domain.ports.out.LoginSecurityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests deterministas: el tiempo se controla con un TimeProvider de reloj fijo
 * (Fase 1 de la normalización temporal), nunca con Instant.now().
 */

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountLockService")
class AccountLockServiceTest {

    private static final Instant FIXED_NOW =
            Instant.parse("2026-09-26T12:00:00Z");

    @Mock
    private LoginSecurityRepository loginSecurityRepository;

    private final TimeProvider timeProvider =
            fixedTimeProvider(FIXED_NOW);

    private AccountLockService accountLockService;

    private final UUID userId = UUID.randomUUID();
    private UserLoginSecurity security;

    private static TimeProvider fixedTimeProvider(Instant instant) {
        Clock clock = Clock.fixed(instant, ZoneOffset.UTC);
        return new TimeProvider() {
            @Override
            public Instant instantNow() {
                return clock.instant();
            }

            @Override
            public java.time.LocalDate localDateNow() {
                return java.time.LocalDate.ofInstant(clock.instant(), clock.getZone());
            }

            @Override
            public ZoneOffset zone() {
                return ZoneOffset.UTC;
            }
        };
    }

    @BeforeEach
    void setUp() {
        accountLockService = new AccountLockService(loginSecurityRepository, timeProvider);
        security = new UserLoginSecurity();
        security.setUserId(userId);
        security.setFailedAttempts(0);
        security.setCreatedAt(FIXED_NOW);
        security.setModifiedAt(FIXED_NOW);
    }

    @Nested
    @DisplayName("checkLockStatus")
    class CheckLockStatus {

        @Test
        @DisplayName("Should do nothing when no record exists")
        void shouldDoNothingWhenNoRecord() {
            when(loginSecurityRepository.findByUserId(userId))
                    .thenReturn(Optional.empty());

            assertThatCode(() ->
                    accountLockService.checkLockStatus(userId)
            ).doesNotThrowAnyException();

            verify(loginSecurityRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw AccountLockedException when blocked")
        void shouldThrowWhenBlocked() {
            security.setFailedAttempts(5);
            security.setBlockedUntil(FIXED_NOW.plus(10, java.time.temporal.ChronoUnit.MINUTES));

            when(loginSecurityRepository.findByUserId(userId))
                    .thenReturn(Optional.of(security));

            assertThatThrownBy(() ->
                    accountLockService.checkLockStatus(userId)
            ).isInstanceOf(AccountLockedException.class);

            verify(loginSecurityRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should unlock when block is expired keeping attempts")
        void shouldUnlockWhenBlockExpired() {
            security.setFailedAttempts(5);
            security.setBlockedUntil(FIXED_NOW.minus(5, java.time.temporal.ChronoUnit.MINUTES));

            when(loginSecurityRepository.findByUserId(userId))
                    .thenReturn(Optional.of(security));

            assertThatCode(() ->
                    accountLockService.checkLockStatus(userId)
            ).doesNotThrowAnyException();

            assertThat(security.getFailedAttempts()).isEqualTo(5);
            assertThat(security.getBlockedUntil()).isNull();
            verify(loginSecurityRepository).save(security);
        }

        @Test
        @DisplayName("Should not persist when record is clean")
        void shouldNotPersistWhenRecordIsClean() {
            security.setFailedAttempts(0);
            security.setBlockedUntil(null);

            when(loginSecurityRepository.findByUserId(userId))
                    .thenReturn(Optional.of(security));

            accountLockService.checkLockStatus(userId);

            verify(loginSecurityRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("handleFailedLogin")
    class HandleFailedLogin {

        @Test
        @DisplayName("Should create record on first failure")
        void shouldCreateRecordOnFirstFailure() {
            when(loginSecurityRepository.findByUserId(userId))
                    .thenReturn(Optional.empty());
            when(loginSecurityRepository.save(any(UserLoginSecurity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            assertThatCode(() ->
                    accountLockService.handleFailedLogin(userId)
            ).doesNotThrowAnyException();

            ArgumentCaptor<UserLoginSecurity> captor =
                    ArgumentCaptor.forClass(UserLoginSecurity.class);
            verify(loginSecurityRepository).save(captor.capture());

            UserLoginSecurity saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(userId);
            assertThat(saved.getFailedAttempts()).isEqualTo(1);
            assertThat(saved.getBlockedUntil()).isNull();
            assertThat(saved.getLastAttemptAt()).isNotNull();
        }

        @Test
        @DisplayName("Should increment without blocking below threshold")
        void shouldIncrementWithoutBlocking() {
            security.setFailedAttempts(3);

            when(loginSecurityRepository.findByUserId(userId))
                    .thenReturn(Optional.of(security));
            when(loginSecurityRepository.save(any(UserLoginSecurity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            assertThatCode(() ->
                    accountLockService.handleFailedLogin(userId)
            ).doesNotThrowAnyException();

            assertThat(security.getFailedAttempts()).isEqualTo(4);
            assertThat(security.getBlockedUntil()).isNull();
        }

        @Test
        @DisplayName("Should block and throw at 5th attempt")
        void shouldBlockAtFifthAttempt() {
            security.setFailedAttempts(4);

            when(loginSecurityRepository.findByUserId(userId))
                    .thenReturn(Optional.of(security));
            when(loginSecurityRepository.save(any(UserLoginSecurity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            assertThatThrownBy(() ->
                    accountLockService.handleFailedLogin(userId)
            ).isInstanceOf(AccountLockedException.class);

            assertThat(security.getFailedAttempts()).isEqualTo(5);
            assertThat(security.getBlockedUntil()).isNotNull();
        }

        @Test
        @DisplayName("Should escalate lock duration on subsequent failures")
        void shouldEscalateLockDuration() {
            security.setFailedAttempts(5);

            when(loginSecurityRepository.findByUserId(userId))
                    .thenReturn(Optional.of(security));
            when(loginSecurityRepository.save(any(UserLoginSecurity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            assertThatThrownBy(() ->
                    accountLockService.handleFailedLogin(userId)
            ).isInstanceOf(AccountLockedException.class);

            assertThat(security.getFailedAttempts()).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("handleSuccessfulLogin")
    class HandleSuccessfulLogin {

        @Test
        @DisplayName("Should do nothing when no record exists")
        void shouldDoNothingWhenNoRecord() {
            when(loginSecurityRepository.findByUserId(userId))
                    .thenReturn(Optional.empty());

            assertThatCode(() ->
                    accountLockService.handleSuccessfulLogin(userId)
            ).doesNotThrowAnyException();

            verify(loginSecurityRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reset attempts when previous failures exist")
        void shouldResetAttemptsWhenFailuresExist() {
            security.setFailedAttempts(3);
            security.setBlockedUntil(FIXED_NOW.minus(1, java.time.temporal.ChronoUnit.MINUTES));

            when(loginSecurityRepository.findByUserId(userId))
                    .thenReturn(Optional.of(security));

            accountLockService.handleSuccessfulLogin(userId);

            assertThat(security.getFailedAttempts()).isZero();
            assertThat(security.getBlockedUntil()).isNull();
            verify(loginSecurityRepository).save(security);
        }

        @Test
        @DisplayName("Should not save when record is clean")
        void shouldNotSaveWhenClean() {
            security.setFailedAttempts(0);
            security.setBlockedUntil(null);

            when(loginSecurityRepository.findByUserId(userId))
                    .thenReturn(Optional.of(security));

            accountLockService.handleSuccessfulLogin(userId);

            verify(loginSecurityRepository, never()).save(any());
        }
    }
}