package com.puntomartinez.millete.users.infrastructure.in.controller.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserSessionResponseDTO(
        UUID id,
        String channel,
        boolean active,
        LocalDateTime createdAt
) {}
