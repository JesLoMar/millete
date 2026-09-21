package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.domain.ports.out.UserSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionPersistenceService")
class SessionPersistenceServiceTest {

    @Mock
    private UserSessionRepository userSessionRepository;

    @InjectMocks
    private SessionPersistenceService sessionPersistenceService;

    private final UUID userId = UUID.randomUUID();

    @Nested
    @DisplayName("createSession")
    class CreateSession {

        @Test
        @DisplayName("Should create active session with new id and channel")
        void shouldCreateActiveSession() {
            when(userSessionRepository.save(any(UserSession.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            UserSession result = sessionPersistenceService.createSession(
                    userId, UserSession.CHANNEL_WEB
            );

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getChannel()).isEqualTo(UserSession.CHANNEL_WEB);
            assertThat(result.isActive()).isTrue();
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getModifiedAt()).isNotNull();
            verify(userSessionRepository).save(any(UserSession.class));
        }

        @Test
        @DisplayName("Should allow multiple sessions for same user")
        void shouldAllowMultipleSessions() {
            when(userSessionRepository.save(any(UserSession.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            UserSession first = sessionPersistenceService.createSession(
                    userId, UserSession.CHANNEL_WEB
            );
            UserSession second = sessionPersistenceService.createSession(
                    userId, UserSession.CHANNEL_WEB
            );

            assertThat(first.getId()).isNotEqualTo(second.getId());
            verify(userSessionRepository, times(2)).save(any(UserSession.class));
        }
    }

    @Nested
    @DisplayName("markSessionAsInactive")
    class MarkSessionAsInactive {

        @Test
        @DisplayName("Should deactivate existing session")
        void shouldDeactivateExistingSession() {
            UUID sessionId = UUID.randomUUID();
            UserSession session = new UserSession();
            session.setId(sessionId);
            session.setUserId(userId);
            session.setChannel(UserSession.CHANNEL_WEB);
            session.setActive(true);

            when(userSessionRepository.findById(sessionId))
                    .thenReturn(Optional.of(session));

            sessionPersistenceService.markSessionAsInactive(sessionId);

            assertThat(session.isActive()).isFalse();
            assertThat(session.getModifiedAt()).isNotNull();
            verify(userSessionRepository).save(session);
        }

        @Test
        @DisplayName("Should do nothing when session not found")
        void shouldDoNothingWhenSessionNotFound() {
            UUID sessionId = UUID.randomUUID();

            when(userSessionRepository.findById(sessionId))
                    .thenReturn(Optional.empty());

            assertThatCode(() ->
                    sessionPersistenceService.markSessionAsInactive(sessionId)
            ).doesNotThrowAnyException();

            verify(userSessionRepository, never()).save(any());
        }
    }
}