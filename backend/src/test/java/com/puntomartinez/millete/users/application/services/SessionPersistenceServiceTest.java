package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.domain.ports.out.UserSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @DisplayName("createSession - crea sesión activa con id nuevo y canal indicado")
    void createSessionShouldCreateActiveSession() {
        when(userSessionRepository.save(any(UserSession.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserSession result = sessionPersistenceService.createSession(userId, SessionPersistenceService.CHANNEL_WEB);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getChannel()).isEqualTo(SessionPersistenceService.CHANNEL_WEB);
        assertThat(result.isActive()).isTrue();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getModifiedAt()).isNotNull();
        verify(userSessionRepository).save(any(UserSession.class));
    }

    @Test
    @DisplayName("createSession - permite múltiples sesiones WEB activas para el mismo usuario")
    void createSessionShouldAllowMultipleWebSessionsPerUser() {
        when(userSessionRepository.save(any(UserSession.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserSession first = sessionPersistenceService.createSession(userId, SessionPersistenceService.CHANNEL_WEB);
        UserSession second = sessionPersistenceService.createSession(userId, SessionPersistenceService.CHANNEL_WEB);

        assertThat(first.getId()).isNotEqualTo(second.getId());
        verify(userSessionRepository, times(2)).save(any(UserSession.class));
    }

    @Test
    @DisplayName("markSessionAsInactive - desactiva la sesión existente")
    void markSessionAsInactiveShouldDeactivateExistingSession() {
        UUID sessionId = UUID.randomUUID();
        UserSession session = new UserSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setChannel(SessionPersistenceService.CHANNEL_WEB);
        session.setActive(true);
        when(userSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        sessionPersistenceService.markSessionAsInactive(sessionId);

        assertThat(session.isActive()).isFalse();
        assertThat(session.getModifiedAt()).isNotNull();
        verify(userSessionRepository).save(session);
    }

    @Test
    @DisplayName("markSessionAsInactive - no hace nada si la sesión no existe")
    void markSessionAsInactiveShouldDoNothingWhenSessionNotFound() {
        UUID sessionId = UUID.randomUUID();
        when(userSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThatCode(() -> sessionPersistenceService.markSessionAsInactive(sessionId))
                .doesNotThrowAnyException();

        verify(userSessionRepository, never()).save(any());
    }
}