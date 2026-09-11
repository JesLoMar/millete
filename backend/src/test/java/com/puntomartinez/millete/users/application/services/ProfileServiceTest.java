package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.shared.domain.exception.AuthenticationFailedException;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceAlreadyExistsException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.model.UserPreferences;
import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.domain.ports.in.ManageProfileUseCase;
import com.puntomartinez.millete.users.domain.ports.out.PasswordHasherPort;
import com.puntomartinez.millete.users.domain.ports.out.UserPreferencesRepository;
import com.puntomartinez.millete.users.domain.ports.out.UserRepository;
import com.puntomartinez.millete.users.domain.ports.out.UserSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService - Servicio de perfil")
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private UserPreferencesRepository userPreferencesRepository;

    @Mock
    private PasswordHasherPort passwordHasher;

    @InjectMocks
    private ProfileService profileService;

    private final UUID userId = UUID.randomUUID();
    private final String rawPassword = "password123";
    private final String hashedPassword = "hashed_123";

    private User createUser() {
        return new User(
                userId,
                "ana",
                "ana@mail.com",
                hashedPassword,
                LocalDateTime.now(),
                LocalDateTime.now(),
                true,
                false
        );
    }

    @Test
    @DisplayName("Obtener perfil de usuario")
    void shouldGetProfile() {
        User user = createUser();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ManageProfileUseCase.UserProfileDTO result = profileService.getProfile(userId);

        assertThat(result.username()).isEqualTo("ana");
        assertThat(result.email()).isEqualTo("ana@mail.com");
    }

    @Test
    @DisplayName("Obtener perfil de usuario inexistente lanza error")
    void shouldThrowWhenUserNotFoundOnGetProfile() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> profileService.getProfile(userId))
                .withMessage("Usuario no encontrado");
    }

    @Test
    @DisplayName("Actualizar perfil con datos válidos")
    void shouldUpdateProfile() {
        User user = createUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordHasher.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(userRepository.findByUsername("nuevo")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nuevo@mail.com")).thenReturn(Optional.empty());

        ManageProfileUseCase.UpdateProfileCommand command = new ManageProfileUseCase.UpdateProfileCommand(
                "nuevo", "nuevo@mail.com", rawPassword);

        profileService.updateProfile(userId, command);

        assertThat(user.getUsername()).isEqualTo("nuevo");
        assertThat(user.getEmail()).isEqualTo("nuevo@mail.com");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Actualizar perfil con contraseña incorrecta lanza error")
    void shouldThrowWhenWrongPasswordOnUpdateProfile() {
        User user = createUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong", hashedPassword)).thenReturn(false);

        ManageProfileUseCase.UpdateProfileCommand command = new ManageProfileUseCase.UpdateProfileCommand(
                null, null, "wrong");

        assertThatExceptionOfType(AuthenticationFailedException.class)
                .isThrownBy(() -> profileService.updateProfile(userId, command))
                .withMessage("Contraseña incorrecta");
    }

    @Test
    @DisplayName("Actualizar perfil con email duplicado lanza error")
    void shouldThrowWhenEmailExistsOnUpdateProfile() {
        User user = createUser();
        UUID otherUserId = UUID.randomUUID();
        User otherUser = new User(otherUserId, "otro", "otro@mail.com", hashedPassword,
                LocalDateTime.now(), LocalDateTime.now(), true, false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordHasher.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(userRepository.findByEmail("otro@mail.com")).thenReturn(Optional.of(otherUser));

        ManageProfileUseCase.UpdateProfileCommand command = new ManageProfileUseCase.UpdateProfileCommand(
                null, "otro@mail.com", rawPassword);

        assertThatExceptionOfType(ResourceAlreadyExistsException.class)
                .isThrownBy(() -> profileService.updateProfile(userId, command))
                .withMessage("El email ya está registrado");
    }

    @Test
    @DisplayName("Actualizar perfil con username duplicado lanza error")
    void shouldThrowWhenUsernameExistsOnUpdateProfile() {
        User user = createUser();
        UUID otherUserId = UUID.randomUUID();
        User otherUser = new User(otherUserId, "existente", "otro@mail.com", hashedPassword,
                LocalDateTime.now(), LocalDateTime.now(), true, false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordHasher.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(userRepository.findByUsername("existente")).thenReturn(Optional.of(otherUser));

        ManageProfileUseCase.UpdateProfileCommand command = new ManageProfileUseCase.UpdateProfileCommand(
                "existente", null, rawPassword);

        assertThatExceptionOfType(ResourceAlreadyExistsException.class)
                .isThrownBy(() -> profileService.updateProfile(userId, command))
                .withMessage("El nombre de usuario ya está en uso");
    }

    @Test
    @DisplayName("Actualizar perfil con email inválido lanza error")
    void shouldThrowWhenInvalidEmailOnUpdateProfile() {
        User user = createUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordHasher.matches(rawPassword, hashedPassword)).thenReturn(true);

        ManageProfileUseCase.UpdateProfileCommand command = new ManageProfileUseCase.UpdateProfileCommand(
                null, "no-es-email", rawPassword);

        assertThatExceptionOfType(InvalidInputException.class)
                .isThrownBy(() -> profileService.updateProfile(userId, command))
                .withMessage("Formato de email inválido");
    }

    @Test
    @DisplayName("Actualizar perfil manteniendo mismo email no verifica duplicado")
    void shouldAllowSameEmailOnUpdateProfile() {
        User user = createUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordHasher.matches(rawPassword, hashedPassword)).thenReturn(true);

        ManageProfileUseCase.UpdateProfileCommand command = new ManageProfileUseCase.UpdateProfileCommand(
                null, "ana@mail.com", rawPassword);

        profileService.updateProfile(userId, command);

        verify(userRepository, never()).findByEmail(any());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Cambiar contraseña correctamente")
    void shouldChangePassword() {
        User user = createUser();
        UUID sessionId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordHasher.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(passwordHasher.hashPassword("newPass123")).thenReturn("newHashed");

        ManageProfileUseCase.ChangePasswordCommand command = new ManageProfileUseCase.ChangePasswordCommand(
                rawPassword, "newPass123", sessionId);

        profileService.changePassword(userId, command);

        assertThat(user.getPassword()).isEqualTo("newHashed");
        verify(userSessionRepository).deactivateAllOtherSessions(userId, sessionId);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Cambiar contraseña con contraseña actual incorrecta lanza error")
    void shouldThrowWhenWrongPasswordOnChangePassword() {
        User user = createUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong", hashedPassword)).thenReturn(false);

        ManageProfileUseCase.ChangePasswordCommand command = new ManageProfileUseCase.ChangePasswordCommand(
                "wrong", "newPass123", UUID.randomUUID());

        assertThatExceptionOfType(AuthenticationFailedException.class)
                .isThrownBy(() -> profileService.changePassword(userId, command))
                .withMessage("Contraseña actual incorrecta");
    }

    @Test
    @DisplayName("Obtener preferencias por defecto")
    void shouldGetDefaultPreferences() {
        when(userPreferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());

        String result = profileService.getPreferences(userId);

        assertThat(result).isEqualTo("{}");
    }

    @Test
    @DisplayName("Obtener preferencias existentes")
    void shouldGetExistingPreferences() {
        UserPreferences prefs = new UserPreferences();
        prefs.setPreferencesJson("{\"theme\":\"dark\"}");
        when(userPreferencesRepository.findByUserId(userId)).thenReturn(Optional.of(prefs));

        String result = profileService.getPreferences(userId);

        assertThat(result).isEqualTo("{\"theme\":\"dark\"}");
    }

    @Test
    @DisplayName("Actualizar preferencias válidas")
    void shouldUpdatePreferences() {
        when(userPreferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());

        profileService.updatePreferences(userId, "{\"theme\":\"dark\"}");

        verify(userPreferencesRepository).save(any(UserPreferences.class));
    }

    @Test
    @DisplayName("Actualizar preferencias con JSON inválido lanza error")
    void shouldThrowWhenInvalidJsonOnUpdatePreferences() {
        assertThatExceptionOfType(InvalidInputException.class)
                .isThrownBy(() -> profileService.updatePreferences(userId, "no es json"))
                .withMessage("Las preferencias deben ser un objeto JSON válido");
    }

    @Test
    @DisplayName("Actualizar preferencias con JSON malformado lanza error")
    void shouldThrowWhenMalformedJsonOnUpdatePreferences() {
        assertThatExceptionOfType(InvalidInputException.class)
                .isThrownBy(() -> profileService.updatePreferences(userId, "{\"theme\":}"))
                .withMessage("Formato de preferencias inválido");
    }

    @Test
    @DisplayName("Obtener sesiones activas")
    void shouldGetActiveSessions() {
        UserSession session = new UserSession();
        session.setId(UUID.randomUUID());
        session.setChannel("WEB");
        session.setActive(true);
        session.setCreatedAt(LocalDateTime.now());

        when(userSessionRepository.findByUserIdAndActiveTrue(userId)).thenReturn(List.of(session));

        List<UserSession> result = profileService.getActiveSessions(userId);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Cerrar sesión propia")
    void shouldCloseOwnSession() {
        UUID sessionId = UUID.randomUUID();
        UserSession session = new UserSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setActive(true);

        when(userSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        profileService.closeSession(userId, sessionId);

        assertThat(session.isActive()).isFalse();
        verify(userSessionRepository).save(session);
    }

    @Test
    @DisplayName("Cerrar sesión de otro usuario lanza error")
    void shouldThrowWhenClosingOtherUserSession() {
        UUID sessionId = UUID.randomUUID();
        UserSession session = new UserSession();
        session.setId(sessionId);
        session.setUserId(UUID.randomUUID());
        session.setActive(true);

        when(userSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThatExceptionOfType(ForbiddenOperationException.class)
                .isThrownBy(() -> profileService.closeSession(userId, sessionId))
                .withMessage("No puedes cerrar una sesión que no te pertenece");
    }

    @Test
    @DisplayName("Cerrar todas las demás sesiones")
    void shouldCloseAllOtherSessions() {
        UUID currentSessionId = UUID.randomUUID();

        profileService.closeAllOtherSessions(userId, currentSessionId);

        verify(userSessionRepository).deactivateAllOtherSessions(userId, currentSessionId);
    }

    @Test
    @DisplayName("Desactivar cuenta correctamente")
    void shouldDeactivateAccount() {
        User user = createUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordHasher.matches(rawPassword, hashedPassword)).thenReturn(true);

        profileService.deactivateAccount(userId, rawPassword);

        assertThat(user.isAnonymized()).isTrue();
        assertThat(user.isActive()).isFalse();
        verify(userSessionRepository).deactivateAllSessions(userId);
        verify(userPreferencesRepository).deleteByUserId(userId);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Desactivar cuenta con contraseña incorrecta lanza error")
    void shouldThrowWhenWrongPasswordOnDeactivate() {
        User user = createUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong", hashedPassword)).thenReturn(false);

        assertThatExceptionOfType(AuthenticationFailedException.class)
                .isThrownBy(() -> profileService.deactivateAccount(userId, "wrong"))
                .withMessage("Contraseña incorrecta");
    }
}
