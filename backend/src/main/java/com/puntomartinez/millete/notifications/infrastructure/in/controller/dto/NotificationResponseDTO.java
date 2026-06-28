package com.puntomartinez.millete.notifications.infrastructure.in.controller.dto;

import com.puntomartinez.millete.notifications.domain.model.NotificationType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record NotificationResponseDTO(
        UUID id,
        NotificationType type,
        String title,
        String message,
        Map<String, Object> metadata,
        boolean read,
        boolean actionRequired,
        LocalDateTime actionedAt,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {}
