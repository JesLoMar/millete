package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.domain.ports.in.ManageUserSessionUseCase;
import com.puntomartinez.millete.users.domain.ports.out.UserSessionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SessionPersistenceService implements ManageUserSessionUseCase {

    private final UserSessionRepository userSessionRepository;
    private final TimeProvider timeProvider;

    public SessionPersistenceService(
            UserSessionRepository userSessionRepository,
            TimeProvider timeProvider) {
        this.userSessionRepository = userSessionRepository;
        this.timeProvider = timeProvider;
    }

    @Override
    public UserSession createSession(UUID userId, String channel) {
        UserSession session = new UserSession();
        session.setId(UUID.randomUUID());
        session.setUserId(userId);
        session.setChannel(channel);
        session.setActive(true);
        session.setCreatedAt(timeProvider.now());
        session.setModifiedAt(timeProvider.now());
        return userSessionRepository.save(session);
    }

    @Override
    public void markSessionAsInactive(UUID sessionId) {
        userSessionRepository.findById(sessionId).ifPresent(session -> {
            session.setActive(false);
            session.setModifiedAt(timeProvider.now());
            userSessionRepository.save(session);
        });
    }
}