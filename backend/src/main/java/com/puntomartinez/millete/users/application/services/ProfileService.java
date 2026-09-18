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
import com.puntomartinez.millete.users.domain.ports.in.ManageProfileUseCase;
import com.puntomartinez.millete.users.domain.ports.in.UpdateProfileCommand;
import com.puntomartinez.millete.users.domain.ports.in.UserProfileResult;
import com.puntomartinez.millete.users.domain.ports.out.PasswordHasherPort;
import com.puntomartinez.millete.users.domain.ports.out.UserPreferencesRepository;
import com.puntomartinez.millete.users.domain.ports.out.UserRepository;
import com.puntomartinez.millete.users.domain.ports.out.UserSessionRepository;
import com.puntomartinez.millete.users.domain.validation.EmailValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProfileService implements ManageProfileUseCase {

    private static final int MAX_PREFERENCES_JSON_LENGTH = 10_000;

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final PasswordHasherPort passwordHasher;
    private final ObjectMapper objectMapper;

    public ProfileService(
            UserRepository userRepository,
            UserSessionRepository userSessionRepository,
            UserPreferencesRepository userPreferencesRepository,
            PasswordHasherPort passwordHasher,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.userPreferencesRepository = userPreferencesRepository;
        this.passwordHasher = passwordHasher;
        this.objectMapper = objectMapper;
    }

    @Override
    public UserProfileResult getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado")
                );

        return new UserProfileResult(
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
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado")
                );

        if (!passwordHasher.matches(command.currentPassword(), user.getPassword())) {
            throw new AuthenticationFailedException("Contraseña incorrecta");
        }

        if (command.newUsername() != null
                && !command.newUsername().equals(user.getUsername())) {
            userRepository.findByUsername(command.newUsername()).ifPresent(u -> {
                if (!u.getId().equals(userId)) {
                    throw new ResourceAlreadyExistsException(
                            "El nombre de usuario ya está en uso"
                    );
                }
            });
        }

        if (command.newEmail() != null
                && !command.newEmail().equals(user.getEmail())) {
            EmailValidator.requireValid(command.newEmail());

            userRepository.findByEmail(command.newEmail()).ifPresent(u -> {
                if (!u.getId().equals(userId)) {
                    throw new ResourceAlreadyExistsException(
                            "El email ya está registrado"
                    );
                }
            });
        }

        String username = command.newUsername() != null
                ? command.newUsername()
                : user.getUsername();

        String email = command.newEmail() != null
                ? command.newEmail()
                : user.getEmail();

        user.updateProfile(username, email);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordCommand command) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado")
                );

        if (!passwordHasher.matches(command.currentPassword(), user.getPassword())) {
            throw new AuthenticationFailedException("Contraseña actual incorrecta");
        }

        if (command.newPassword() == null
                || command.newPassword().length() < 8
                || command.newPassword().length() > 100) {
            throw new InvalidInputException(
                    "La nueva contraseña debe tener entre 8 y 100 caracteres"
            );
        }

        user.updatePassword(passwordHasher.hashPassword(command.newPassword()));
        userRepository.save(user);

        userSessionRepository.deactivateAllOtherSessions(
                userId,
                command.currentSessionId()
        );
    }

    @Override
    public Map<String, Object> getPreferences(UUID userId) {
        return userPreferencesRepository.findByUserId(userId)
                .map(UserPreferences::getPreferences)
                .orElse(new HashMap<>());
    }

    @Override
    @Transactional
    public void updatePreferences(
            UUID userId,
            Map<String, Object> preferences
    ) {
        Map<String, Object> safePreferences = preferences != null
                ? preferences
                : new HashMap<>();

        String serialized;
        try {
            serialized = objectMapper.writeValueAsString(safePreferences);
        } catch (JsonProcessingException e) {
            throw new InvalidInputException(
                    "Formato de preferencias inválido"
            );
        }

        if (serialized.length() > MAX_PREFERENCES_JSON_LENGTH) {
            throw new InvalidInputException(
                    "Las preferencias no pueden exceder "
                            + MAX_PREFERENCES_JSON_LENGTH
                            + " caracteres."
            );
        }

        UserPreferences userPreferences =
                userPreferencesRepository.findByUserId(userId)
                        .orElseGet(() -> {
                            UserPreferences newPrefs = new UserPreferences();
                            newPrefs.setId(UUID.randomUUID());
                            newPrefs.setUserId(userId);
                            newPrefs.setCreatedAt(LocalDateTime.now());
                            return newPrefs;
                        });

        userPreferences.setPreferences(safePreferences);
        userPreferences.setModifiedAt(LocalDateTime.now());
        userPreferencesRepository.save(userPreferences);
    }

    @Override
    public List<UserSession> getActiveSessions(UUID userId) {
        return userSessionRepository.findByUserIdAndActiveTrue(userId);
    }

    @Override
    @Transactional
    public void closeSession(UUID userId, UUID sessionIdToClose) {
        UserSession session = userSessionRepository.findById(sessionIdToClose)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Sesión no encontrada")
                );

        if (!session.getUserId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "No puedes cerrar una sesión que no te pertenece"
            );
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
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado")
                );

        if (!passwordHasher.matches(password, user.getPassword())) {
            throw new AuthenticationFailedException("Contraseña incorrecta");
        }

        user.anonymize();
        userRepository.save(user);

        userSessionRepository.deactivateAllSessions(userId);
        userPreferencesRepository.deleteByUserId(userId);
    }
}