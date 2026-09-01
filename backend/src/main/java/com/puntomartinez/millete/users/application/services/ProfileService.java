package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.model.UserPreferences;
import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.domain.ports.in.ManageProfileUseCase;
import com.puntomartinez.millete.users.domain.ports.out.PasswordHasherPort;
import com.puntomartinez.millete.users.domain.ports.out.UserPreferencesRepository;
import com.puntomartinez.millete.users.domain.ports.out.UserRepository;
import com.puntomartinez.millete.users.domain.ports.out.UserSessionRepository;
import com.puntomartinez.millete.shared.domain.exception.AuthenticationFailedException;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceAlreadyExistsException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProfileService implements ManageProfileUseCase {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final PasswordHasherPort passwordHasher;

    public ProfileService(UserRepository userRepository,
                          UserSessionRepository userSessionRepository,
                          UserPreferencesRepository userPreferencesRepository,
                          PasswordHasherPort passwordHasher) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.userPreferencesRepository = userPreferencesRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public UserProfileDTO getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return new UserProfileDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isActive(),
                user.isAnonymized()
        );
    }

    @Override
    @Transactional
    public void updateProfile(UUID userId, UpdateProfileCommand command) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (!passwordHasher.matches(command.currentPassword(), user.getPassword())) {
            throw new AuthenticationFailedException("Contraseña incorrecta");
        }
        if (command.newUsername() != null && !command.newUsername().isBlank()) {
            if (!command.newUsername().equals(user.getUsername())) {
                userRepository.findByUsername(command.newUsername()).ifPresent(u -> {
                    if (!u.getId().equals(userId)) {
                        throw new ResourceAlreadyExistsException("El nombre de usuario ya está en uso");
                    }
                });
            }
            user.setUsername(command.newUsername());
        }
        if (command.newEmail() != null && !command.newEmail().isBlank()) {
            if (!command.newEmail().equals(user.getEmail())) {
                if (!command.newEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    throw new InvalidInputException("Formato de email inválido");
                }
                userRepository.findByEmail(command.newEmail()).ifPresent(u -> {
                    if (!u.getId().equals(userId)) {
                        throw new ResourceAlreadyExistsException("El email ya está registrado");
                    }
                });
            }
            user.setEmail(command.newEmail());
        }
        user.setModifiedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordCommand command) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (!passwordHasher.matches(command.currentPassword(), user.getPassword())) {
            throw new AuthenticationFailedException("Contraseña actual incorrecta");
        }
        user.setPassword(passwordHasher.hashPassword(command.newPassword()));
        user.setModifiedAt(LocalDateTime.now());
        userRepository.save(user);

        userSessionRepository.deactivateAllOtherSessions(userId, command.currentSessionId());
    }

    @Override
    public String getPreferences(UUID userId) {
        return userPreferencesRepository.findByUserId(userId)
                .map(UserPreferences::getPreferencesJson)
                .orElse("{}");
    }

    @Override
    @Transactional
    public void updatePreferences(UUID userId, String preferencesJson) {
        String trimmed = preferencesJson != null ? preferencesJson.trim() : "{}";
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            throw new InvalidInputException("Las preferencias deben ser un objeto JSON válido");
        }
        try {
            new com.fasterxml.jackson.databind.ObjectMapper().readTree(trimmed);
        } catch (Exception e) {
            throw new InvalidInputException("Formato de preferencias inválido");
        }

        UserPreferences preferences = userPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserPreferences newPrefs = new UserPreferences();
                    newPrefs.setId(UUID.randomUUID());
                    newPrefs.setUserId(userId);
                    newPrefs.setCreatedAt(LocalDateTime.now());
                    return newPrefs;
                });
        preferences.setPreferencesJson(trimmed);
        preferences.setModifiedAt(LocalDateTime.now());
        userPreferencesRepository.save(preferences);
    }

    @Override
    public List<UserSession> getActiveSessions(UUID userId) {
        return userSessionRepository.findByUserIdAndActiveTrue(userId);
    }

    @Override
    @Transactional
    public void closeSession(UUID userId, UUID sessionIdToClose) {
        UserSession session = userSessionRepository.findById(sessionIdToClose)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada"));
        if (!session.getUserId().equals(userId)) {
            throw new ForbiddenOperationException("No puedes cerrar una sesión que no te pertenece");
        }
        session.setActive(false);
        session.setModifiedAt(LocalDateTime.now());
        userSessionRepository.save(session);
    }

    @Override
    @Transactional
    public void closeAllOtherSessions(UUID userId, UUID currentSessionId) {
        userSessionRepository.deactivateAllOtherSessions(userId, currentSessionId);
    }

    @Override
    @Transactional
    public void deactivateAccount(UUID userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (!passwordHasher.matches(password, user.getPassword())) {
            throw new AuthenticationFailedException("Contraseña incorrecta");
        }
        user.anonymize();
        userRepository.save(user);
        userSessionRepository.deactivateAllSessions(userId);
        userPreferencesRepository.deleteByUserId(userId);
    }
}
