package com.puntomartinez.millete.users.domain.ports.in;

public interface LoginUserUseCase {
    String login(LoginUserCommand command);
    record LoginUserCommand(String identifier, String rawPassword) {}
}