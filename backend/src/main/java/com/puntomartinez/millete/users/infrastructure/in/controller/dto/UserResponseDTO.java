package com.puntomartinez.millete.users.infrastructure.in.controller.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String username,
        String email,
        Instant createdAt,
        Instant modifiedAt,
        boolean active,
        boolean anonymized
) {}
