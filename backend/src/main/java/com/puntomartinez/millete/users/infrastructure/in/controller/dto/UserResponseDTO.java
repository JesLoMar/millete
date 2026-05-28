package com.puntomartinez.millete.users.infrastructure.in.controller.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String username,
        String email,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        boolean active,
        boolean anonymized
) {}