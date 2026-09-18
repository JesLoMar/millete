package com.puntomartinez.millete.users.domain.ports.in;

import com.puntomartinez.millete.users.domain.model.UserSession;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ManageProfileUseCase {

    UserProfileResult getProfile(UUID userId);

    void updateProfile(UUID userId, UpdateProfileCommand command);

    void changePassword(UUID userId, ChangePasswordCommand command);

    Map<String, Object> getPreferences(UUID userId);

    void updatePreferences(UUID userId, Map<String, Object> preferences);

    List<UserSession> getActiveSessions(UUID userId);

    void closeSession(UUID userId, UUID sessionIdToClose);

    void closeAllOtherSessions(UUID userId, UUID currentSessionId);

    void deactivateAccount(UUID userId, String password);
}