package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.users.domain.exception.AccountLockedException;
import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.domain.ports.out.UserSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AccountLockService {

    private static final long LOCK_DURATION_MINUTES = 15;
    public static final String CHANNEL_WEB = "WEB";

    private final UserSessionRepository userSessionRepository;
    private final SessionPersistenceService sessionPersistenceService;

    public AccountLockService(UserSessionRepository userSessionRepository,
                              SessionPersistenceService sessionPersistenceService) {
        this.userSessionRepository = userSessionRepository;
        this.sessionPersistenceService = sessionPersistenceService;
    }

    public void checkLockStatus(UUID userId) {
        userSessionRepository.findByUserIdAndChannel(userId, CHANNEL_WEB).ifPresent(session -> {
            if (session.isBlocked()) {
                throw new AccountLockedException(session.getBlockedUntil(), calculateRemainingMinutes(session.getBlockedUntil()));
            }
            sessionPersistenceService.saveSession(session);
        });
    }

    public void handleFailedLogin(UUID userId) {
        UserSession session = sessionPersistenceService.persistFailedAttempt(userId);

        if (session.isBlocked()) {
            throw new AccountLockedException(session.getBlockedUntil(), calculateRemainingMinutes(session.getBlockedUntil()));
        }
    }

    @Transactional
    public void handleSuccessfulLogin(UUID userId) {
        userSessionRepository.findByUserIdAndChannel(userId, CHANNEL_WEB).ifPresent(session -> {
            if (session.getLoginAttempts() > 0 || session.isBlocked()) {
                session.resetAttempts();
                userSessionRepository.save(session);
            }
        });
    }

    private long calculateRemainingMinutes(LocalDateTime blockedUntil) {
        if (blockedUntil == null) {
            return LOCK_DURATION_MINUTES;
        }
        long seconds = Duration.between(LocalDateTime.now(), blockedUntil).getSeconds();
        return Math.max(0, (seconds + 59) / 60);
    }
}