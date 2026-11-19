package com.puntomartinez.millete.users.domain.ports.in;

import com.puntomartinez.millete.users.domain.model.User;

public interface RegisterUserUseCase {
    User register(RegisterUserCommand command);


    record RegisterUserCommand(String username, String email, String rawPassword, String timezone) {
        public RegisterUserCommand(String username, String email, String rawPassword) {
            this(username, email, rawPassword, null);
        }
    }
}
