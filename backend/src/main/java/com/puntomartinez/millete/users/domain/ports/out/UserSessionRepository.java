package com.puntomartinez.millete.users.domain.ports.out;

import com.puntomartinez.millete.users.domain.model.UserSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository {
    List<UserSession> findByUserIdAndChannel(UUID userId, String channel);
    Optional<UserSession> findById(UUID id);
    UserSession save(UserSession session);
    boolean existsByIdAndActiveTrue(UUID id);
    List<UserSession> findByUserIdAndActiveTrue(UUID userId);
    void deactivateAllOtherSessions(UUID userId, UUID currentSessionId);
    void deactivateAllSessions(UUID userId);
}
