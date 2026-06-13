package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.users.domain.exception.AccountLockedException;
import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.domain.ports.out.UserSessionRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountLockService - Servicio de bloqueo de cuentas")
class AccountLockServiceTest {

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private SessionPersistenceService sessionPersistenceService;

    @InjectMocks
    private AccountLockService accountLockService;

    private final UUID userId = UUID.randomUUID();
    private UserSession session;

    @BeforeEach
    void setUp() {
        session = new UserSession();
        session.setId(UUID.randomUUID());
        session.setUserId(userId);
        session.setChannel(AccountLockService.CHANNEL_WEB);
        session.setLoginAttempts(0);
        session.setCreatedAt(LocalDateTime.now());
        session.setModifiedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("checkLockStatus - sesión no existe no hace nada")
    void checkLockStatusShouldDoNothingWhenSessionNotFound() {
        when(userSessionRepository.findByUserIdAndChannel(userId, AccountLockService.CHANNEL_WEB))
                .thenReturn(Optional.empty());

        assertThatCode(() -> accountLockService.checkLockStatus(userId))
                .doesNotThrowAnyException();

        verify(userSessionRepository).findByUserIdAndChannel(userId, AccountLockService.CHANNEL_WEB);
        verify(sessionPersistenceService, never()).saveSession(any());
    }

    @Test
    @DisplayName("checkLockStatus - sesión no bloqueada guarda cambios pendientes")
    void checkLockStatusShouldSaveWhenNotBlocked() {
        when(userSessionRepository.findByUserIdAndChannel(userId, AccountLockService.CHANNEL_WEB))
                .thenReturn(Optional.of(session));

        assertThatCode(() -> accountLockService.checkLockStatus(userId))
                .doesNotThrowAnyException();

        verify(sessionPersistenceService).saveSession(session);
    }

    @Test
    @DisplayName("checkLockStatus - sesión bloqueada lanza AccountLockedException")
    void checkLockStatusShouldThrowWhenBlocked() {
        session.setLoginAttempts(5);
        session.setBlockedUntil(LocalDateTime.now().plusMinutes(10));

        when(userSessionRepository.findByUserIdAndChannel(userId, AccountLockService.CHANNEL_WEB))
                .thenReturn(Optional.of(session));

        assertThatThrownBy(() -> accountLockService.checkLockStatus(userId))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("Inténtalo de nuevo en");

        verify(sessionPersistenceService, never()).saveSession(any());
    }

    @Test
    @DisplayName("checkLockStatus - bloqueo expirado realiza desbloqueo perezoso y guarda")
    void checkLockStatusShouldUnlockWhenBlockExpired() {
        session.setLoginAttempts(5);
        session.setBlockedUntil(LocalDateTime.now().minusMinutes(5)); // Ya expiró

        when(userSessionRepository.findByUserIdAndChannel(userId, AccountLockService.CHANNEL_WEB))
                .thenReturn(Optional.of(session));

        assertThatCode(() -> accountLockService.checkLockStatus(userId))
                .doesNotThrowAnyException();

        // Verificar que se hizo el desbloqueo perezoso
        assertThat(session.getLoginAttempts()).isZero();
        assertThat(session.getBlockedUntil()).isNull();

        verify(sessionPersistenceService).saveSession(session);
    }

    @Test
    @DisplayName("handleFailedLogin - delega en SessionPersistenceService y no lanza si no está bloqueado")
    void handleFailedLoginShouldDelegateAndNotThrowWhenNotBlocked() {
        session.setLoginAttempts(3); // 4º intento, no alcanza límite
        session.registerFailedAttempt(5, 15); // Simulamos 4º fallo

        when(sessionPersistenceService.persistFailedAttempt(userId)).thenReturn(session);

        assertThatCode(() -> accountLockService.handleFailedLogin(userId))
                .doesNotThrowAnyException();

        verify(sessionPersistenceService).persistFailedAttempt(userId);
    }

    @Test
    @DisplayName("handleFailedLogin - lanza AccountLockedException cuando se alcanza el límite")
    void handleFailedLoginShouldThrowWhenBlocked() {
        session.setLoginAttempts(5);
        session.setBlockedUntil(LocalDateTime.now().plusMinutes(15));

        when(sessionPersistenceService.persistFailedAttempt(userId)).thenReturn(session);

        assertThatThrownBy(() -> accountLockService.handleFailedLogin(userId))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("Inténtalo de nuevo en 15 minutos");

        verify(sessionPersistenceService).persistFailedAttempt(userId);
    }

    @Test
    @DisplayName("handleSuccessfulLogin - no hace nada si no existe sesión")
    void handleSuccessfulLoginShouldDoNothingWhenSessionNotFound() {
        when(userSessionRepository.findByUserIdAndChannel(userId, AccountLockService.CHANNEL_WEB))
                .thenReturn(Optional.empty());

        assertThatCode(() -> accountLockService.handleSuccessfulLogin(userId))
                .doesNotThrowAnyException();

        verify(userSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("handleSuccessfulLogin - resetea intentos si existen fallos previos")
    void handleSuccessfulLoginShouldResetAttempts() {
        session.setLoginAttempts(3);
        session.setBlockedUntil(LocalDateTime.now().plusMinutes(5));

        when(userSessionRepository.findByUserIdAndChannel(userId, AccountLockService.CHANNEL_WEB))
                .thenReturn(Optional.of(session));

        accountLockService.handleSuccessfulLogin(userId);

        assertThat(session.getLoginAttempts()).isZero();
        assertThat(session.getBlockedUntil()).isNull();

        verify(userSessionRepository).save(session);
    }

    @Test
    @DisplayName("handleSuccessfulLogin - no guarda si no hay intentos previos")
    void handleSuccessfulLoginShouldNotSaveWhenClean() {
        session.setLoginAttempts(0);
        session.setBlockedUntil(null);

        when(userSessionRepository.findByUserIdAndChannel(userId, AccountLockService.CHANNEL_WEB))
                .thenReturn(Optional.of(session));

        accountLockService.handleSuccessfulLogin(userId);

        verify(userSessionRepository, never()).save(any());
    }
}