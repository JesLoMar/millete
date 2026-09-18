package com.puntomartinez.millete.users.domain.ports.in;

public record AuthenticateUserCommand(
        String identifier,
        String rawPassword
) {
}