package com.puntomartinez.millete.users.domain.ports.in;

import com.puntomartinez.millete.users.domain.model.User;

public interface LoginUserUseCase {
    User login(LoginUserCommand command);
    record LoginUserCommand(String identifier, String rawPassword) {}
}
