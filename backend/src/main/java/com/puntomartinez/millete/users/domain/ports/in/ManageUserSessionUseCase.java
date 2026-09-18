package com.puntomartinez.millete.users.domain.ports.in;

import com.puntomartinez.millete.users.domain.model.UserSession;

import java.util.UUID;

public interface ManageUserSessionUseCase {

    UserSession createSession(UUID userId, String channel);

    void markSessionAsInactive(UUID sessionId);
}