package com.puntomartinez.millete.users.domain.ports.in;

public interface AuthenticateUserUseCase {

    AuthenticationResult authenticate(AuthenticateUserCommand command);
}