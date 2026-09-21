package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.domain.ports.in.AuthenticateUserCommand;
import com.puntomartinez.millete.users.domain.ports.in.AuthenticationResult;
import com.puntomartinez.millete.users.domain.ports.in.LoginUserUseCase;
import com.puntomartinez.millete.users.domain.ports.in.ManageUserSessionUseCase;
import com.puntomartinez.millete.users.domain.ports.out.TokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService")
class AuthenticationServiceTest {

    @Mock
    private LoginUserUseCase loginUserUseCase;

    @Mock
    private ManageUserSessionUseCase manageUserSessionUseCase;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Should authenticate and return user, session and JWT")
    void shouldAuthenticateAndReturnResult() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);

        UserSession session = mock(UserSession.class);
        UUID sessionId = UUID.randomUUID();
        when(session.getId()).thenReturn(sessionId);

        AuthenticateUserCommand command =
                new AuthenticateUserCommand("ana@mail.com", "password123");

        when(loginUserUseCase.login(
                any(LoginUserUseCase.LoginUserCommand.class)
        )).thenReturn(user);
        when(manageUserSessionUseCase.createSession(
                userId, UserSession.CHANNEL_WEB
        )).thenReturn(session);
        when(tokenProvider.generateToken(user, sessionId))
                .thenReturn("jwt-token");

        AuthenticationResult result =
                authenticationService.authenticate(command);

        assertThat(result.user()).isEqualTo(user);
        assertThat(result.session()).isEqualTo(session);
        assertThat(result.jwt()).isEqualTo("jwt-token");

        verify(loginUserUseCase).login(any());
        verify(manageUserSessionUseCase)
                .createSession(userId, UserSession.CHANNEL_WEB);
        verify(tokenProvider).generateToken(user, sessionId);
    }

    @Test
    @DisplayName("Should propagate exception from login use case")
    void shouldPropagateLoginException() {
        AuthenticateUserCommand command =
                new AuthenticateUserCommand("ana@mail.com", "wrong");

        when(loginUserUseCase.login(any()))
                .thenThrow(new RuntimeException("Login failed"));

        assertThatThrownBy(() ->
                authenticationService.authenticate(command)
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Login failed");
    }
}