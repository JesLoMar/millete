package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.domain.ports.out.UserSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SessionPersistenceService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;
    public static final String CHANNEL_WEB = "WEB";

    private final UserSessionRepository userSessionRepository;

    public SessionPersistenceService(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserSession createSession(UUID userId, String channel) {
        UserSession session = new UserSession();
        session.setId(UUID.randomUUID());
        session.setUserId(userId);
        session.setChannel(channel);
        session.setActive(true);
        session.setLoginAttempts(0);
        session.setBlockedUntil(null);
        session.setCreatedAt(LocalDateTime.now());
        session.setModifiedAt(LocalDateTime.now());
        return userSessionRepository.save(session);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSessionAsInactive(UUID sessionId) {
        userSessionRepository.findById(sessionId).ifPresent(session -> {
            session.setActive(false);
            session.setModifiedAt(LocalDateTime.now());
            userSessionRepository.save(session);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserSession persistFailedAttempt(UUID userId) {
        UserSession session = getOrCreateSession(userId, CHANNEL_WEB);
        session.registerFailedAttempt(MAX_ATTEMPTS, LOCK_DURATION_MINUTES);
        return userSessionRepository.save(session);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSession(UserSession session) {
        userSessionRepository.save(session);
    }

    private UserSession getOrCreateSession(UUID userId, String channel) {
        List<UserSession> sessions = userSessionRepository.findByUserIdAndChannel(userId, channel);
        return sessions.stream()
                .filter(UserSession::isActive)
                .findFirst()
                .orElseGet(() -> {
                    UserSession newSession = new UserSession();
                    newSession.setId(UUID.randomUUID());
                    newSession.setUserId(userId);
                    newSession.setChannel(channel);
                    newSession.setActive(true);
                    newSession.setLoginAttempts(0);
                    newSession.setCreatedAt(LocalDateTime.now());
                    newSession.setModifiedAt(LocalDateTime.now());
                    return newSession;
                });
    }
}