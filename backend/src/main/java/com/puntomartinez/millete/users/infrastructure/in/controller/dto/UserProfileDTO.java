package com.puntomartinez.millete.users.infrastructure.in.controller.dto;

import java.util.UUID;

public record UserProfileDTO(
        UUID id,
        String username,
        String email,
        boolean active,
        boolean anonymized
) {
}