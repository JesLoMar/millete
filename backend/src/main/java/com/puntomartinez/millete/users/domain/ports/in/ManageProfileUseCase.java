package com.puntomartinez.millete.users.domain.ports.in;

import com.puntomartinez.millete.users.domain.model.UserSession;

import java.util.List;
import java.util.UUID;

public interface ManageProfileUseCase {
    UserProfileDTO getProfile(UUID userId);
    void updateProfile(UUID userId, UpdateProfileCommand command);
    void changePassword(UUID userId, ChangePasswordCommand command);
    String getPreferences(UUID userId);
    void updatePreferences(UUID userId, String preferencesJson);
    void unlinkTelegram(UUID userId);
    List<UserSession> getActiveSessions(UUID userId);
    void closeSession(UUID userId, UUID sessionIdToClose);
    void closeAllOtherSessions(UUID userId, UUID currentSessionId);
    void deactivateAccount(UUID userId, String password);

    record UserProfileDTO(UUID id, String username, String email, boolean active, boolean anonymized, Long telegramChatId) {}
    record UpdateProfileCommand(String newUsername, String newEmail, String currentPassword) {}
    record ChangePasswordCommand(String currentPassword, String newPassword, UUID currentSessionId) {}
}
