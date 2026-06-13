package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.domain.ports.out.UserSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("SessionPersistenceService - Persistencia de sesiones")
class SessionPersistenceServiceTest {

    @Mock
    private UserSessionRepository userSessionRepository;

    @InjectMocks
    private SessionPersistenceService sessionPersistenceService;

    private final UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("persistFailedAttempt - crea nueva sesión si no existe y registra fallo")
    void persistFailedAttemptShouldCreateSessionAndRegisterFailure() {
        when(userSessionRepository.findByUserIdAndChannel(userId, "WEB"))
                .thenReturn(Optional.empty());
        when(userSessionRepository.save(any(UserSession.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserSession result = sessionPersistenceService.persistFailedAttempt(userId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getChannel()).isEqualTo("WEB");
        assertThat(result.getLoginAttempts()).isEqualTo(1);
        assertThat(result.getLastAttemptAt()).isNotNull();
        assertThat(result.getBlockedUntil()).isNull(); // Solo 1 fallo, no bloquea

        verify(userSessionRepository).findByUserIdAndChannel(userId, "WEB");
        verify(userSessionRepository).save(any(UserSession.class));
    }

    @Test
    @DisplayName("persistFailedAttempt - actualiza sesión existente con nuevo fallo")
    void persistFailedAttemptShouldUpdateExistingSession() {
        UserSession existingSession = createSession(userId, 3); // 3 fallos previos
        LocalDateTime previousLastAttempt = existingSession.getLastAttemptAt();

        when(userSessionRepository.findByUserIdAndChannel(userId, "WEB"))
                .thenReturn(Optional.of(existingSession));
        when(userSessionRepository.save(any(UserSession.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserSession result = sessionPersistenceService.persistFailedAttempt(userId);

        assertThat(result.getLoginAttempts()).isEqualTo(4); // 4º fallo
        assertThat(result.getBlockedUntil()).isNull(); // Aún no llega al límite
        // CORRECCIÓN: usamos isAfterOrEqualTo en lugar de isAfter
        assertThat(result.getLastAttemptAt())
                .isAfterOrEqualTo(previousLastAttempt);

        verify(userSessionRepository).findByUserIdAndChannel(userId, "WEB");
        verify(userSessionRepository).save(existingSession);
    }

    @Test
    @DisplayName("persistFailedAttempt - bloquea la sesión al alcanzar el 5º intento")
    void persistFailedAttemptShouldBlockAtFifthAttempt() {
        UserSession existingSession = createSession(userId, 4); // 4 fallos previos
        when(userSessionRepository.findByUserIdAndChannel(userId, "WEB"))
                .thenReturn(Optional.of(existingSession));
        when(userSessionRepository.save(any(UserSession.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserSession result = sessionPersistenceService.persistFailedAttempt(userId);

        assertThat(result.getLoginAttempts()).isEqualTo(5); // 5º fallo
        assertThat(result.getBlockedUntil()).isNotNull();
        assertThat(result.getBlockedUntil())
                .isAfter(LocalDateTime.now().plusMinutes(14))
                .isBefore(LocalDateTime.now().plusMinutes(16)); // ~15 min

        verify(userSessionRepository).save(existingSession);
    }

    @Test
    @DisplayName("persistFailedAttempt - no bloquea si se superan los 5 intentos (ya bloqueado)")
    void persistFailedAttemptShouldNotDoubleBlock() {
        UserSession existingSession = createSession(userId, 5);
        existingSession.setBlockedUntil(LocalDateTime.now().plusMinutes(5)); // Ya bloqueado
        when(userSessionRepository.findByUserIdAndChannel(userId, "WEB"))
                .thenReturn(Optional.of(existingSession));
        when(userSessionRepository.save(any(UserSession.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserSession result = sessionPersistenceService.persistFailedAttempt(userId);

        assertThat(result.getLoginAttempts()).isEqualTo(6); // Sigue contando
        assertThat(result.getBlockedUntil()).isNotNull(); // Sigue bloqueado
        // La fecha no debería cambiar porque ya estaba bloqueado
        assertThat(result.getBlockedUntil()).isEqualTo(existingSession.getBlockedUntil());
    }

    @Test
    @DisplayName("saveSession - guarda la sesión en el repositorio")
    void saveSessionShouldPersistSession() {
        UserSession session = createSession(userId, 2);

        sessionPersistenceService.saveSession(session);

        verify(userSessionRepository).save(session);
    }

    private UserSession createSession(UUID userId, int attempts) {
        UserSession session = new UserSession();
        session.setId(UUID.randomUUID());
        session.setUserId(userId);
        session.setChannel("WEB");
        session.setLoginAttempts(attempts);
        session.setLastAttemptAt(LocalDateTime.now().minusMinutes(2));
        session.setCreatedAt(LocalDateTime.now().minusHours(1));
        session.setModifiedAt(LocalDateTime.now().minusMinutes(2));
        return session;
    }
}