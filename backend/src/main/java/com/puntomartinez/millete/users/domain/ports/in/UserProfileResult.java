package com.puntomartinez.millete.users.domain.ports.in;

import java.util.UUID;

public record UserProfileResult(
        UUID id,
        String username,
        String email,
        boolean active,
        boolean anonymized
) {
}