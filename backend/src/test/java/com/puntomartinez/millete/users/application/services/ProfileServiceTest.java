package com.puntomartinez.millete.users.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntomartinez.millete.shared.domain.exception.AuthenticationFailedException;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceAlreadyExistsException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.model.UserPreferences;
import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.domain.ports.in.ChangePasswordCommand;
import com.puntomartinez.millete.users.domain.ports.in.UpdateProfileCommand;
import com.puntomartinez.millete.users.domain.ports.in.UserProfileResult;
import com.puntomartinez.millete.users.domain.ports.out.PasswordHasherPort;
import com.puntomartinez.millete.users.domain.ports.out.UserPreferencesRepository;
import com.puntomartinez.millete.users.domain.ports.out.UserRepository;
import com.puntomartinez.millete.users.domain.ports.out.UserSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService")
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private UserPreferencesRepository userPreferencesRepository;

    @Mock
    private PasswordHasherPort passwordHasher;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProfileService profileService;

    private final UUID userId = UUID.randomUUID();
    private final String rawPassword = "password123";
    private final String hashedPassword = "hashed_123";

    private User createUser() {
        return new User(
                userId, "ana", "ana@mail.com", hashedPassword,
                LocalDateTime.now(), LocalDateTime.now(), true, false
        );
    }

    @Nested
    @DisplayName("getProfile")
    class GetProfile {

        @Test
        @DisplayName("Should return user profile")
        void shouldGetProfile() {
            User user = createUser();
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));

            UserProfileResult result =
                    profileService.getProfile(userId);

            assertThat(result.id()).isEqualTo(userId);
            assertThat(result.username()).isEqualTo("ana");
            assertThat(result.email()).isEqualTo("ana@mail.com");
            assertThat(result.active()).isTrue();
            assertThat(result.anonymized()).isFalse();
        }

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    profileService.getProfile(userId)
            ).isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("Should update profile with valid data")
        void shouldUpdateProfile() {
            User user = createUser();
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));
            when(passwordHasher.matches(rawPassword, hashedPassword))
                    .thenReturn(true);
            when(userRepository.findByUsername("nuevo"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmail("nuevo@mail.com"))
                    .thenReturn(Optional.empty());

            UpdateProfileCommand command =
                    new UpdateProfileCommand(
                            "nuevo", "nuevo@mail.com", rawPassword
                    );

            profileService.updateProfile(userId, command);

            assertThat(user.getUsername()).isEqualTo("nuevo");
            assertThat(user.getEmail()).isEqualTo("nuevo@mail.com");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw on wrong password")
        void shouldThrowOnWrongPassword() {
            User user = createUser();
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));
            when(passwordHasher.matches("wrong", hashedPassword))
                    .thenReturn(false);

            UpdateProfileCommand command =
                    new UpdateProfileCommand(null, null, "wrong");

            assertThatThrownBy(() ->
                    profileService.updateProfile(userId, command)
            ).isInstanceOf(AuthenticationFailedException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw on duplicate email")
        void shouldThrowOnDuplicateEmail() {
            User user = createUser();
            UUID otherUserId = UUID.randomUUID();
            User otherUser = new User(
                    otherUserId, "otro", "otro@mail.com",
                    hashedPassword,
                    LocalDateTime.now(), LocalDateTime.now(),
                    true, false
            );

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));
            when(passwordHasher.matches(rawPassword, hashedPassword))
                    .thenReturn(true);
            when(userRepository.findByEmail("otro@mail.com"))
                    .thenReturn(Optional.of(otherUser));

            UpdateProfileCommand command =
                    new UpdateProfileCommand(
                            null, "otro@mail.com", rawPassword
                    );

            assertThatThrownBy(() ->
                    profileService.updateProfile(userId, command)
            ).isInstanceOf(ResourceAlreadyExistsException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw on duplicate username")
        void shouldThrowOnDuplicateUsername() {
            User user = createUser();
            UUID otherUserId = UUID.randomUUID();
            User otherUser = new User(
                    otherUserId, "existente", "otro@mail.com",
                    hashedPassword,
                    LocalDateTime.now(), LocalDateTime.now(),
                    true, false
            );

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));
            when(passwordHasher.matches(rawPassword, hashedPassword))
                    .thenReturn(true);
            when(userRepository.findByUsername("existente"))
                    .thenReturn(Optional.of(otherUser));

            UpdateProfileCommand command =
                    new UpdateProfileCommand(
                            "existente", null, rawPassword
                    );

            assertThatThrownBy(() ->
                    profileService.updateProfile(userId, command)
            ).isInstanceOf(ResourceAlreadyExistsException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw on invalid email format")
        void shouldThrowOnInvalidEmail() {
            User user = createUser();
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));
            when(passwordHasher.matches(rawPassword, hashedPassword))
                    .thenReturn(true);

            UpdateProfileCommand command =
                    new UpdateProfileCommand(
                            null, "no-es-email", rawPassword
                    );

            assertThatThrownBy(() ->
                    profileService.updateProfile(userId, command)
            ).isInstanceOf(InvalidInputException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow same email without duplicate check")
        void shouldAllowSameEmailWithoutDuplicateCheck() {
            User user = createUser();
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));
            when(passwordHasher.matches(rawPassword, hashedPassword))
                    .thenReturn(true);

            UpdateProfileCommand command =
                    new UpdateProfileCommand(
                            null, "ana@mail.com", rawPassword
                    );

            profileService.updateProfile(userId, command);

            verify(userRepository, never()).findByEmail(any());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should allow same username without duplicate check")
        void shouldAllowSameUsernameWithoutDuplicateCheck() {
            User user = createUser();
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));
            when(passwordHasher.matches(rawPassword, hashedPassword))
                    .thenReturn(true);

            UpdateProfileCommand command =
                    new UpdateProfileCommand(
                            "ana", null, rawPassword
                    );

            profileService.updateProfile(userId, command);

            verify(userRepository, never()).findByUsername(any());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should not update when both username and email are null")
        void shouldNotUpdateWhenBothNull() {
            User user = createUser();
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));
            when(passwordHasher.matches(rawPassword, hashedPassword))
                    .thenReturn(true);

            UpdateProfileCommand command =
                    new UpdateProfileCommand(null, null, rawPassword);

            assertThatCode(() -> profileService.updateProfile(userId, command))
                    .doesNotThrowAnyException();

            verify(userRepository).save(user);
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("Should change password and deactivate other sessions")
        void shouldChangePassword() {
            User user = createUser();
            UUID sessionId = UUID.randomUUID();

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));
            when(passwordHasher.matches(rawPassword, hashedPassword))
                    .thenReturn(true);
            when(passwordHasher.hashPassword("newPass123"))
                    .thenReturn("newHashed");

            ChangePasswordCommand command =
                    new ChangePasswordCommand(
                            rawPassword, "newPass123", sessionId
                    );

            profileService.changePassword(userId, command);

            assertThat(user.getPassword()).isEqualTo("newHashed");
            verify(userSessionRepository)
                    .deactivateAllOtherSessions(userId, sessionId);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw on wrong current password")
        void shouldThrowOnWrongCurrentPassword() {
            User user = createUser();
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));
            when(passwordHasher.matches("wrong", hashedPassword))
                    .thenReturn(false);

            ChangePasswordCommand command =
                    new ChangePasswordCommand(
                            "wrong", "newPass123", UUID.randomUUID()
                    );

            assertThatThrownBy(() ->
                    profileService.changePassword(userId, command)
            ).isInstanceOf(AuthenticationFailedException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject new password shorter than 8 chars")
        void shouldRejectShortNewPassword() {
            User user = createUser();
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));
            when(passwordHasher.matches(rawPassword, hashedPassword))
                    .thenReturn(true);

            ChangePasswordCommand command =
                    new ChangePasswordCommand(
                            rawPassword, "short", UUID.randomUUID()
                    );

            assertThatThrownBy(() ->
                    profileService.changePassword(userId, command)
            ).isInstanceOf(InvalidInputException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow changing to same password")
        void shouldAllowChangingToSamePassword() {
            User user = createUser();
            UUID sessionId = UUID.randomUUID();

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));
            when(passwordHasher.matches(rawPassword, hashedPassword))
                    .thenReturn(true);
            when(passwordHasher.hashPassword(rawPassword))
                    .thenReturn(hashedPassword);

            ChangePasswordCommand command =
                    new ChangePasswordCommand(
                            rawPassword, rawPassword, sessionId
                    );

            assertThatCode(() -> profileService.changePassword(userId, command))
                    .doesNotThrowAnyException();

            verify(userRepository).save(user);
        }
    }

    @Nested
    @DisplayName("preferences")
    class Preferences {

        @Test
        @DisplayName("Should return empty map when no preferences exist")
        void shouldReturnEmptyMapWhenNoPreferences() {
            when(userPreferencesRepository.findByUserId(userId))
                    .thenReturn(Optional.empty());

            Map<String, Object> result =
                    profileService.getPreferences(userId);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return existing preferences")
        void shouldReturnExistingPreferences() {
            UserPreferences prefs = new UserPreferences();
            prefs.setPreferences(new HashMap<>(Map.of("theme", "dark")));

            when(userPreferencesRepository.findByUserId(userId))
                    .thenReturn(Optional.of(prefs));

            Map<String, Object> result =
                    profileService.getPreferences(userId);

            assertThat(result).containsEntry("theme", "dark");
        }

        @Test
        @DisplayName("Should update preferences creating new record")
        void shouldUpdatePreferencesCreatingNewRecord() throws Exception {
            Map<String, Object> newPrefs = Map.of("theme", "dark");

            when(userPreferencesRepository.findByUserId(userId))
                    .thenReturn(Optional.empty());
            when(objectMapper.writeValueAsString(any()))
                    .thenReturn("{\"theme\":\"dark\"}");

            profileService.updatePreferences(userId, newPrefs);

            verify(userPreferencesRepository)
                    .save(any(UserPreferences.class));
        }

        @Test
        @DisplayName("Should update preferences on existing record")
        void shouldUpdatePreferencesOnExistingRecord() throws Exception {
            Map<String, Object> newPrefs = Map.of("theme", "light");
            UserPreferences existingPrefs = new UserPreferences();
            existingPrefs.setUserId(userId);
            existingPrefs.setPreferences(
                    new HashMap<>(Map.of("theme", "dark"))
            );

            when(userPreferencesRepository.findByUserId(userId))
                    .thenReturn(Optional.of(existingPrefs));
            when(objectMapper.writeValueAsString(any()))
                    .thenReturn("{\"theme\":\"light\"}");

            profileService.updatePreferences(userId, newPrefs);

            assertThat(existingPrefs.getPreferences())
                    .containsEntry("theme", "light");
            verify(userPreferencesRepository).save(existingPrefs);
        }

        @Test
        @DisplayName("Should throw on serialization error")
        void shouldThrowOnSerializationError() throws Exception {
            Map<String, Object> prefs = Map.of("key", "value");

            when(objectMapper.writeValueAsString(any()))
                    .thenThrow(new JsonProcessingException("error") {});

            assertThatThrownBy(() ->
                    profileService.updatePreferences(userId, prefs)
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should handle null preferences as empty map")
        void shouldHandleNullPreferencesAsEmptyMap() throws Exception {
            when(userPreferencesRepository.findByUserId(userId))
                    .thenReturn(Optional.empty());
            when(objectMapper.writeValueAsString(any()))
                    .thenReturn("{}");

            profileService.updatePreferences(userId, null);

            verify(userPreferencesRepository)
                    .save(any(UserPreferences.class));
        }
    }

    @Nested
    @DisplayName("sessions")
    class Sessions {

        @Test
        @DisplayName("Should return active sessions")
        void shouldGetActiveSessions() {
            UserSession session = new UserSession();
            session.setId(UUID.randomUUID());
            session.setChannel("WEB");
            session.setActive(true);
            session.setCreatedAt(LocalDateTime.now());

            when(userSessionRepository.findByUserIdAndActiveTrue(userId))
                    .thenReturn(List.of(session));

            List<UserSession> result =
                    profileService.getActiveSessions(userId);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty list when no active sessions")
        void shouldReturnEmptyListWhenNoActiveSessions() {
            when(userSessionRepository.findByUserIdAndActiveTrue(userId))
                    .thenReturn(List.of());

            List<UserSession> result =
                    profileService.getActiveSessions(userId);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should close own session")
        void shouldCloseOwnSession() {
            UUID sessionId = UUID.randomUUID();
            UserSession session = new UserSession();
            session.setId(sessionId);
            session.setUserId(userId);
            session.setActive(true);

            when(userSessionRepository.findById(sessionId))
                    .thenReturn(Optional.of(session));

            profileService.closeSession(userId, sessionId);

            assertThat(session.isActive()).isFalse();
            verify(userSessionRepository).save(session);
        }

        @Test
        @DisplayName("Should throw when closing other user session")
        void shouldThrowWhenClosingOtherUserSession() {
            UUID sessionId = UUID.randomUUID();
            UserSession session = new UserSession();
            session.setId(sessionId);
            session.setUserId(UUID.randomUUID());
            session.setActive(true);

            when(userSessionRepository.findById(sessionId))
                    .thenReturn(Optional.of(session));

            assertThatThrownBy(() ->
                    profileService.closeSession(userId, sessionId)
            ).isInstanceOf(ForbiddenOperationException.class);

            verify(userSessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when session not found")
        void shouldThrowWhenSessionNotFound() {
            UUID sessionId = UUID.randomUUID();

            when(userSessionRepository.findById(sessionId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    profileService.closeSession(userId, sessionId)
            ).isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should not persist when session is already inactive")
        void shouldNotPersistWhenAlreadyInactive() {
            UUID sessionId = UUID.randomUUID();
            UserSession session = new UserSession();
            session.setId(sessionId);
            session.setUserId(userId);
            session.setActive(false);

            when(userSessionRepository.findById(sessionId))
                    .thenReturn(Optional.of(session));

            profileService.closeSession(userId, sessionId);

            verify(userSessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should close all other sessions")
        void shouldCloseAllOtherSessions() {
            UUID currentSessionId = UUID.randomUUID();

            profileService.closeAllOtherSessions(userId, currentSessionId);

            verify(userSessionRepository)
                    .deactivateAllOtherSessions(userId, currentSessionId);
        }
    }

    @Nested
    @DisplayName("deactivateAccount")
    class DeactivateAccount {

        @Test
        @DisplayName("Should anonymize and deactivate account")
        void shouldDeactivateAccount() {
            User user = createUser();
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));
            when(passwordHasher.matches(rawPassword, hashedPassword))
                    .thenReturn(true);

            profileService.deactivateAccount(userId, rawPassword);

            assertThat(user.isAnonymized()).isTrue();
            assertThat(user.isActive()).isFalse();
            verify(userSessionRepository).deactivateAllSessions(userId);
            verify(userPreferencesRepository).deleteByUserId(userId);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw on wrong password for deactivation")
        void shouldThrowOnWrongPasswordForDeactivation() {
            User user = createUser();
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));
            when(passwordHasher.matches("wrong", hashedPassword))
                    .thenReturn(false);

            assertThatThrownBy(() ->
                    profileService.deactivateAccount(userId, "wrong")
            ).isInstanceOf(AuthenticationFailedException.class);

            verify(userRepository, never()).save(any());
            verify(userSessionRepository, never()).deactivateAllSessions(any());
        }
    }
}