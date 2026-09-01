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
}