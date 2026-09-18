package com.puntomartinez.millete.users.domain.ports.in;

public record UpdateProfileCommand(
        String newUsername,
        String newEmail,
        String currentPassword
) {
}