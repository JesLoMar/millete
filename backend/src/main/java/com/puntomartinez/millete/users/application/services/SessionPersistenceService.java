package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.domain.ports.in.ManageUserSessionUseCase;
import com.puntomartinez.millete.users.domain.ports.out.UserSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SessionPersistenceService implements ManageUserSessionUseCase {

    private final UserSessionRepository userSessionRepository;
    private final TimeProvider timeProvider;

    public SessionPersistenceService(UserSessionRepository userSessionRepository,
            TimeProvider timeProvider
    ) {
        this.timeProvider = timeProvider;
        this.userSessionRepository = userSessionRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserSession createSession(UUID userId, String channel) {
        UserSession session = new UserSession();
        session.setId(UUID.randomUUID());
        session.setUserId(userId);
        session.setChannel(channel);
        session.setActive(true);
        session.setCreatedAt(timeProvider.instantNow());
        session.setModifiedAt(timeProvider.instantNow());
        return userSessionRepository.save(session);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSessionAsInactive(UUID sessionId) {
        userSessionRepository.findById(sessionId).ifPresent(session -> {
            session.setActive(false);
            session.setModifiedAt(timeProvider.instantNow());
            userSessionRepository.save(session);
        });
    }
}