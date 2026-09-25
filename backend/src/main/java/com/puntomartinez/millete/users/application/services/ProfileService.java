package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.shared.domain.time.TimeProvider;
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
import com.puntomartinez.millete.users.domain.validation.ZoneIdValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProfileService implements ManageProfileUseCase {

    private static final int MAX_PREFERENCES_JSON_LENGTH = 10_000;

    private final UserRepository userRepository;
    private final TimeProvider timeProvider;
    private final UserSessionRepository userSessionRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final PasswordHasherPort passwordHasher;
    private final ObjectMapper objectMapper;

    public ProfileService(
            UserRepository userRepository,
            UserSessionRepository userSessionRepository,
            UserPreferencesRepository userPreferencesRepository,
            PasswordHasherPort passwordHasher,
            ObjectMapper objectMapper,
            TimeProvider timeProvider
    ) {
        this.timeProvider = timeProvider;
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.userPreferencesRepository = userPreferencesRepository;
        this.passwordHasher = passwordHasher;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResult getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario no encontrado"
                        )
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
                        new ResourceNotFoundException(
                                "Usuario no encontrado"
                        )
                );

        if (!passwordHasher.matches(
                command.currentPassword(),
                user.getPassword()
        )) {
            throw new AuthenticationFailedException(
                    "Contraseña incorrecta"
            );
        }

        String newUsername = normalizeOptional(command.newUsername());
        String newEmail = normalizeOptional(command.newEmail());

        if (newUsername != null
                && !newUsername.equals(user.getUsername())) {
            userRepository.findByUsername(newUsername)
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(userId)) {
                            throw new ResourceAlreadyExistsException(
                                    "El nombre de usuario ya está en uso"
                            );
                        }
                    });
        }

        if (newEmail != null
                && !newEmail.equals(user.getEmail())) {
            EmailValidator.requireValid(newEmail);

            userRepository.findByEmail(newEmail)
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(userId)) {
                            throw new ResourceAlreadyExistsException(
                                    "El email ya está registrado"
                            );
                        }
                    });
        }

        String username = newUsername != null
                ? newUsername
                : user.getUsername();

        String email = newEmail != null
                ? newEmail
                : user.getEmail();

        user.updateProfile(timeProvider, username, email);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordCommand command) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario no encontrado"
                        )
                );

        if (!passwordHasher.matches(
                command.currentPassword(),
                user.getPassword()
        )) {
            throw new AuthenticationFailedException(
                    "Contraseña actual incorrecta"
            );
        }

        if (command.newPassword() == null
                || command.newPassword().length() < 8
                || command.newPassword().length() > 100) {
            throw new InvalidInputException(
                    "La nueva contraseña debe tener entre 8 y 100 caracteres"
            );
        }

        user.updatePassword(
                timeProvider,
                passwordHasher.hashPassword(command.newPassword())
        );

        userRepository.save(user);

        userSessionRepository.deactivateAllOtherSessions(
                userId,
                command.currentSessionId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPreferences(UUID userId) {
        return userPreferencesRepository.findByUserId(userId)
                .map(prefs -> withDefaultTimezone(prefs.getPreferences()))
                .orElseGet(() -> withDefaultTimezone(new HashMap<>()));
    }

    @Override
    @Transactional
    public void updatePreferences(
            UUID userId,
            Map<String, Object> preferences
    ) {
        Map<String, Object> safePreferences = preferences != null
                ? new HashMap<>(preferences)
                : new HashMap<>();

        // Fase 1 (normalización temporal): la clave "timezone" es una
        // preferencia de dominio con validación estricta. Se normaliza y
        // valida antes de persistir; si no se envía, se preserva el valor
        // existente o se aplica el default técnico UTC.
        Object timezoneValue = safePreferences.get(UserPreferences.TIMEZONE_KEY);
        if (timezoneValue == null) {
            String existingTimezone = userPreferencesRepository.findByUserId(userId)
                    .map(UserPreferences::getTimezoneOrDefault)
                    .orElse(ZoneIdValidator.DEFAULT_TIMEZONE);
            safePreferences.put(UserPreferences.TIMEZONE_KEY, existingTimezone);
        } else {
            if (!(timezoneValue instanceof String)) {
                throw new InvalidInputException(
                        "La zona horaria debe ser un texto con un identificador IANA válido"
                );
            }
            safePreferences.put(
                    UserPreferences.TIMEZONE_KEY,
                    ZoneIdValidator.normalizeOrDefault((String) timezoneValue)
            );
        }

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
                            UserPreferences newPreferences =
                                    new UserPreferences();

                            newPreferences.setId(UUID.randomUUID());
                            newPreferences.setUserId(userId);
                            newPreferences.setCreatedAt(timeProvider.instantNow());

                            return newPreferences;
                        });

        userPreferences.setPreferences(safePreferences);
        userPreferences.setModifiedAt(timeProvider.instantNow());

        userPreferencesRepository.save(userPreferences);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSession> getActiveSessions(UUID userId) {
        return userSessionRepository.findByUserIdAndActiveTrue(userId);
    }

    @Override
    @Transactional
    public void closeSession(UUID userId, UUID sessionIdToClose) {
        UserSession session = userSessionRepository
                .findById(sessionIdToClose)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Sesión no encontrada"
                        )
                );

        if (!session.getUserId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "No puedes cerrar una sesión que no te pertenece"
            );
        }

        if (!session.isActive()) {
            return;
        }

        session.setActive(false);
        session.setModifiedAt(timeProvider.instantNow());

        userSessionRepository.save(session);
    }

    @Override
    @Transactional
    public void closeAllOtherSessions(
            UUID userId,
            UUID currentSessionId
    ) {
        userSessionRepository.deactivateAllOtherSessions(
                userId,
                currentSessionId
        );
    }

    @Override
    @Transactional
    public void deactivateAccount(UUID userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario no encontrado"
                        )
                );

        if (!passwordHasher.matches(password, user.getPassword())) {
            throw new AuthenticationFailedException(
                    "Contraseña incorrecta"
            );
        }

        user.anonymize(timeProvider);
        userRepository.save(user);

        userSessionRepository.deactivateAllSessions(userId);
        userPreferencesRepository.deleteByUserId(userId);
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    /**
     * Copia defensiva de las preferencias asegurando que la clave "timezone"
     * esté siempre presente en la respuesta API (default técnico: UTC).
     */
    private Map<String, Object> withDefaultTimezone(Map<String, Object> preferences) {
        Map<String, Object> copy = preferences != null
                ? new HashMap<>(preferences)
                : new HashMap<>();

        Object timezone = copy.get(UserPreferences.TIMEZONE_KEY);
        if (!(timezone instanceof String s) || s.isBlank()) {
            copy.put(UserPreferences.TIMEZONE_KEY, ZoneIdValidator.DEFAULT_TIMEZONE);
        }
        return copy;
    }
}