package com.puntomartinez.millete.users.domain.ports.in;

import com.puntomartinez.millete.users.domain.model.User;

public interface RegisterUserUseCase {
    User register(RegisterUserCommand command);


    record RegisterUserCommand(String username, String email, String rawPassword) {}
}
