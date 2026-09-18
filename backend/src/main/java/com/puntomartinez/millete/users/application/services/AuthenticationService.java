package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.domain.ports.in.AuthenticateUserCommand;
import com.puntomartinez.millete.users.domain.ports.in.AuthenticateUserUseCase;
import com.puntomartinez.millete.users.domain.ports.in.AuthenticationResult;
import com.puntomartinez.millete.users.domain.ports.in.LoginUserUseCase;
import com.puntomartinez.millete.users.domain.ports.in.ManageUserSessionUseCase;
import com.puntomartinez.millete.users.domain.ports.out.TokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService implements AuthenticateUserUseCase {

    private final LoginUserUseCase loginUserUseCase;
    private final ManageUserSessionUseCase manageUserSessionUseCase;
    private final TokenProvider tokenProvider;

    public AuthenticationService(
            LoginUserUseCase loginUserUseCase,
            ManageUserSessionUseCase manageUserSessionUseCase,
            TokenProvider tokenProvider
    ) {
        this.loginUserUseCase = loginUserUseCase;
        this.manageUserSessionUseCase = manageUserSessionUseCase;
        this.tokenProvider = tokenProvider;
    }

    @Override
    @Transactional
    public AuthenticationResult authenticate(AuthenticateUserCommand command) {
        LoginUserUseCase.LoginUserCommand loginCommand =
                new LoginUserUseCase.LoginUserCommand(
                        command.identifier(),
                        command.rawPassword()
                );

        User user = loginUserUseCase.login(loginCommand);

        UserSession session = manageUserSessionUseCase.createSession(
                user.getId(),
                UserSession.CHANNEL_WEB
        );

        String jwt = tokenProvider.generateToken(user, session.getId());

        return new AuthenticationResult(user, session, jwt);
    }
}