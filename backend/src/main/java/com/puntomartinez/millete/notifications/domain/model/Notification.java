package com.puntomartinez.millete.notifications.domain.model;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;

import java.time.Instant;
import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import java.util.Map;
import java.util.UUID;

public class Notification {

    private static final int MAX_TITLE_LENGTH = 255;

    private final UUID id;
    private final UUID userId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final Map<String, Object> metadata;
    private boolean read;
    private final boolean actionRequired;
    private Instant actionedAt;
    private final Instant createdAt;
    private final Instant expiresAt;
    private boolean active;

    private Notification(
            UUID id,
            UUID userId,
            NotificationType type,
            String title,
            String message,
            Map<String, Object> metadata,
            boolean read,
            boolean actionRequired,
            Instant actionedAt,
            Instant createdAt,
            Instant expiresAt,
            boolean active
    ) {
        validateId(id);
        validateUserId(userId);
        validateType(type);
        validateTitle(title);
        validateCreatedAt(createdAt);

        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.metadata = metadata == null
                ? null
                : Map.copyOf(metadata);
        this.read = read;
        this.actionRequired = actionRequired;
        this.actionedAt = actionedAt;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.active = active;
    }

    public static Notification create(
            TimeProvider timeProvider,
            UUID userId,
            NotificationType type,
            String title,
            String message,
            Map<String, Object> metadata,
            boolean actionRequired,
            Instant expiresAt
    ) {
        return new Notification(
                UUID.randomUUID(),
                userId,
                type,
                title,
                message,
                metadata,
                false,
                actionRequired,
                null,
                timeProvider.instantNow(),
                expiresAt,
                true
        );
    }

    public static Notification reconstitute(
            UUID id,
            UUID userId,
            NotificationType type,
            String title,
            String message,
            Map<String, Object> metadata,
            boolean read,
            boolean actionRequired,
            Instant actionedAt,
            Instant createdAt,
            Instant expiresAt,
            boolean active
    ) {
        return new Notification(
                id,
                userId,
                type,
                title,
                message,
                metadata,
                read,
                actionRequired,
                actionedAt,
                createdAt,
                expiresAt,
                active
        );
    }

    public boolean markAsRead() {
        if (this.read) {
            return false;
        }

        this.read = true;
        return true;
    }

    public boolean markAsActioned(TimeProvider timeProvider) {
        if (!this.actionRequired) {
            throw new InvalidInputException(
                    "La notificación no requiere ninguna acción."
            );
        }

        if (this.actionedAt != null) {
            return false;
        }

        this.actionedAt = timeProvider.instantNow();
        return true;
    }

    public void softDelete() {
        this.active = false;
    }

    public boolean isExpired(TimeProvider timeProvider) {
        return expiresAt != null
                && timeProvider.instantNow().isAfter(expiresAt);
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new InvalidInputException(
                    "El id de la notificación es obligatorio."
            );
        }
    }

    private static void validateUserId(UUID userId) {
        if (userId == null) {
            throw new InvalidInputException(
                    "El userId de la notificación es obligatorio."
            );
        }
    }

    private static void validateType(NotificationType type) {
        if (type == null) {
            throw new InvalidInputException(
                    "El tipo de notificación es obligatorio."
            );
        }
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new InvalidInputException(
                    "El título de la notificación es obligatorio."
            );
        }

        if (title.length() > MAX_TITLE_LENGTH) {
            throw new InvalidInputException(
                    "El título de la notificación no puede superar los "
                            + MAX_TITLE_LENGTH + " caracteres."
            );
        }
    }

    private static void validateCreatedAt(Instant createdAt) {
        if (createdAt == null) {
            throw new InvalidInputException(
                    "La fecha de creación de la notificación es obligatoria."
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public boolean isRead() {
        return read;
    }

    public boolean isActionRequired() {
        return actionRequired;
    }

    public Instant getActionedAt() {
        return actionedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isActive() {
        return active;
    }
}