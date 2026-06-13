package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.domain.ports.out.UserSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        return userSessionRepository.findByUserIdAndChannel(userId, channel)
                .orElseGet(() -> {
                    UserSession newSession = new UserSession();
                    newSession.setId(UUID.randomUUID());
                    newSession.setUserId(userId);
                    newSession.setChannel(channel);
                    newSession.setLoginAttempts(0);
                    newSession.setCreatedAt(LocalDateTime.now());
                    newSession.setModifiedAt(LocalDateTime.now());
                    return newSession;
                });
    }
}