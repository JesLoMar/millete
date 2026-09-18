package com.puntomartinez.millete.users.domain.ports.in;

import java.util.UUID;

public record ChangePasswordCommand(
        String currentPassword,
        String newPassword,
        UUID currentSessionId
) {
}