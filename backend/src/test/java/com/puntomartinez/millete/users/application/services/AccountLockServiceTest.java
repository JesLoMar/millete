package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.users.domain.exception.AccountLockedException;
import com.puntomartinez.millete.users.domain.model.UserLoginSecurity;
import com.puntomartinez.millete.users.domain.ports.out.LoginSecurityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountLockService - Servicio de bloqueo de cuentas")
class AccountLockServiceTest {

    @Mock
    private LoginSecurityRepository loginSecurityRepository;

    @InjectMocks
    private AccountLockService accountLockService;

    private final UUID userId = UUID.randomUUID();
    private UserLoginSecurity security;

    @BeforeEach
    void setUp() {
        security = new UserLoginSecurity();
        security.setUserId(userId);
        security.setFailedAttempts(0);
        security.setCreatedAt(LocalDateTime.now());
        security.setModifiedAt(LocalDateTime.now());
    }

    // ==================== checkLockStatus ====================

    @Test
    @DisplayName("checkLockStatus - usuario sin registro de seguridad no hace nada")
    void checkLockStatusShouldDoNothingWhenNoRecord() {
        when(loginSecurityRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatCode(() -> accountLockService.checkLockStatus(userId))
                .doesNotThrowAnyException();

        verify(loginSecurityRepository).findByUserId(userId);
        verify(loginSecurityRepository, never()).save(any());
    }

    @Test
    @DisplayName("checkLockStatus - cuenta bloqueada lanza AccountLockedException")
    void checkLockStatusShouldThrowWhenBlocked() {
        security.setFailedAttempts(5);
        security.setBlockedUntil(LocalDateTime.now().plusMinutes(10));
        when(loginSecurityRepository.findByUserId(userId)).thenReturn(Optional.of(security));

        assertThatThrownBy(() -> accountLockService.checkLockStatus(userId))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("Inténtalo de nuevo en");

        verify(loginSecurityRepository, never()).save(any());
    }

    @Test
    @DisplayName("checkLockStatus - bloqueo expirado realiza desbloqueo perezoso y guarda")
    void checkLockStatusShouldUnlockWhenBlockExpired() {
        security.setFailedAttempts(5);
        security.setBlockedUntil(LocalDateTime.now().minusMinutes(5));
        when(loginSecurityRepository.findByUserId(userId)).thenReturn(Optional.of(security));

        assertThatCode(() -> accountLockService.checkLockStatus(userId))
                .doesNotThrowAnyException();

        assertThat(security.getFailedAttempts()).isZero();
        assertThat(security.getBlockedUntil()).isNull();
        verify(loginSecurityRepository).save(security);
    }

    @Test
    @DisplayName("checkLockStatus - registro limpio no persiste nada")
    void checkLockStatusShouldNotSaveWhenRecordIsClean() {
        // Dado: un usuario sin intentos ni bloqueo (lo habitual en cada login)
        UUID userId = UUID.randomUUID();
        UserLoginSecurity security = new UserLoginSecurity();
        security.setUserId(userId);
        security.setFailedAttempts(0);
        security.setBlockedUntil(null);
        when(loginSecurityRepository.findByUserId(userId)).thenReturn(Optional.of(security));

        // Cuando
        accountLockService.checkLockStatus(userId);

        // Entonces: no hay nada que persistir — ni un solo write innecesario
        verify(loginSecurityRepository, never()).save(any());
    }

    // ==================== handleFailedLogin ====================

    @Test
    @DisplayName("handleFailedLogin - crea registro si no existe y cuenta el primer fallo")
    void handleFailedLoginShouldCreateRecordOnFirstFailure() {
        when(loginSecurityRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(loginSecurityRepository.save(any(UserLoginSecurity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> accountLockService.handleFailedLogin(userId))
                .doesNotThrowAnyException();

        ArgumentCaptor<UserLoginSecurity> captor = ArgumentCaptor.forClass(UserLoginSecurity.class);
        verify(loginSecurityRepository).save(captor.capture());
        UserLoginSecurity saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getFailedAttempts()).isEqualTo(1);
        assertThat(saved.getBlockedUntil()).isNull();
        assertThat(saved.getLastAttemptAt()).isNotNull();
    }

    @Test
    @DisplayName("handleFailedLogin - incrementa intentos sin bloquear por debajo del límite")
    void handleFailedLoginShouldIncrementWithoutBlocking() {
        security.setFailedAttempts(3);
        when(loginSecurityRepository.findByUserId(userId)).thenReturn(Optional.of(security));
        when(loginSecurityRepository.save(any(UserLoginSecurity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> accountLockService.handleFailedLogin(userId))
                .doesNotThrowAnyException();

        assertThat(security.getFailedAttempts()).isEqualTo(4);
        assertThat(security.getBlockedUntil()).isNull();
        verify(loginSecurityRepository).save(security);
    }

    @Test
    @DisplayName("handleFailedLogin - bloquea y lanza AccountLockedException al 5º intento")
    void handleFailedLoginShouldBlockAtFifthAttempt() {
        security.setFailedAttempts(4);
        when(loginSecurityRepository.findByUserId(userId)).thenReturn(Optional.of(security));
        when(loginSecurityRepository.save(any(UserLoginSecurity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> accountLockService.handleFailedLogin(userId))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("Inténtalo de nuevo en 15 minutos");

        assertThat(security.getFailedAttempts()).isEqualTo(5);
        assertThat(security.getBlockedUntil())
                .isAfter(LocalDateTime.now().plusMinutes(14))
                .isBefore(LocalDateTime.now().plusMinutes(16));
        verify(loginSecurityRepository).save(security);
    }

    // ==================== handleSuccessfulLogin ====================

    @Test
    @DisplayName("handleSuccessfulLogin - no hace nada si no existe registro")
    void handleSuccessfulLoginShouldDoNothingWhenNoRecord() {
        when(loginSecurityRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatCode(() -> accountLockService.handleSuccessfulLogin(userId))
                .doesNotThrowAnyException();

        verify(loginSecurityRepository, never()).save(any());
    }

    @Test
    @DisplayName("handleSuccessfulLogin - resetea intentos si existen fallos previos")
    void handleSuccessfulLoginShouldResetAttempts() {
        security.setFailedAttempts(3);
        security.setBlockedUntil(LocalDateTime.now().minusMinutes(1));
        when(loginSecurityRepository.findByUserId(userId)).thenReturn(Optional.of(security));

        accountLockService.handleSuccessfulLogin(userId);

        assertThat(security.getFailedAttempts()).isZero();
        assertThat(security.getBlockedUntil()).isNull();
        verify(loginSecurityRepository).save(security);
    }

    @Test
    @DisplayName("handleSuccessfulLogin - no guarda si no hay intentos previos")
    void handleSuccessfulLoginShouldNotSaveWhenClean() {
        security.setFailedAttempts(0);
        security.setBlockedUntil(null);
        when(loginSecurityRepository.findByUserId(userId)).thenReturn(Optional.of(security));

        accountLockService.handleSuccessfulLogin(userId);

        verify(loginSecurityRepository, never()).save(any());
    }
}