package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.users.domain.exception.AccountLockedException;
import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.ports.in.LoginUserUseCase;
import com.puntomartinez.millete.users.domain.ports.in.RegisterUserUseCase;
import com.puntomartinez.millete.users.domain.ports.out.PasswordHasherPort;
import com.puntomartinez.millete.users.domain.ports.out.TokenProvider;
import com.puntomartinez.millete.users.domain.ports.out.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Servicio de usuarios")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasherPort passwordHasher;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private AccountLockService accountLockService;

    @InjectMocks
    private UserService userService;

    private final UUID defaultUserId = UUID.randomUUID();
    private final String rawPassword = "password123";
    private final String hashedPassword = "hashed_123";
    private final String email = "ana@mail.com";
    private final String username = "ana";
    private final String token = "jwt_token";

    @Test
    @DisplayName("Registrar usuario con email")
    void shouldRegisterUserWithEmail() {
        when(passwordHasher.hashPassword(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        RegisterUserUseCase.RegisterUserCommand command = new RegisterUserUseCase.RegisterUserCommand(
                null, email, rawPassword);

        User result = userService.register(command);

        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getPassword()).isEqualTo(hashedPassword);
        assertThat(result.isActive()).isTrue();
        verify(passwordHasher).hashPassword(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Registrar usuario con username")
    void shouldRegisterUserWithUsername() {
        when(passwordHasher.hashPassword(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        RegisterUserUseCase.RegisterUserCommand command = new RegisterUserUseCase.RegisterUserCommand(
                username, null, rawPassword);

        User result = userService.register(command);

        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("Registrar usuario sin identificador lanza error")
    void shouldThrowWhenNoIdentifier() {
        RegisterUserUseCase.RegisterUserCommand command = new RegisterUserUseCase.RegisterUserCommand(
                null, null, rawPassword);

        assertThatRuntimeException()
                .isThrownBy(() -> userService.register(command))
                .withMessage("Se requiere un email o un nombre de usuario para registrarse.");
    }

    @Test
    @DisplayName("Registrar con email duplicado lanza error")
    void shouldThrowWhenEmailExists() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mock(User.class)));

        RegisterUserUseCase.RegisterUserCommand command = new RegisterUserUseCase.RegisterUserCommand(
                null, email, rawPassword);

        assertThatRuntimeException()
                .isThrownBy(() -> userService.register(command))
                .withMessage("El email " + email + " ya está registrado.");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar con username duplicado lanza error")
    void shouldThrowWhenUsernameExists() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mock(User.class)));

        RegisterUserUseCase.RegisterUserCommand command = new RegisterUserUseCase.RegisterUserCommand(
                username, null, rawPassword);

        assertThatRuntimeException()
                .isThrownBy(() -> userService.register(command))
                .withMessage("El nombre de usuario " + username + " ya está en uso.");
    }

    @Test
    @DisplayName("Login con credenciales correctas resetea intentos y devuelve token")
    void shouldLoginSuccessfully() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(defaultUserId);
        when(user.getPassword()).thenReturn(hashedPassword);
        when(passwordHasher.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(userRepository.findByIdentifier(email)).thenReturn(Optional.of(user));
        when(tokenProvider.generateToken(user)).thenReturn(token);

        LoginUserUseCase.LoginUserCommand command = new LoginUserUseCase.LoginUserCommand(email, rawPassword);

        String result = userService.login(command);

        assertThat(result).isEqualTo(token);
        verify(accountLockService).checkLockStatus(defaultUserId);
        verify(accountLockService).handleSuccessfulLogin(defaultUserId);
        verify(tokenProvider).generateToken(user);
    }

    @Test
    @DisplayName("Login con password incorrecta registra fallo y lanza error")
    void shouldThrowWhenWrongPassword() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(defaultUserId);
        when(user.getPassword()).thenReturn(hashedPassword);
        when(passwordHasher.matches("wrong", hashedPassword)).thenReturn(false);
        when(userRepository.findByIdentifier(email)).thenReturn(Optional.of(user));

        LoginUserUseCase.LoginUserCommand command = new LoginUserUseCase.LoginUserCommand(email, "wrong");

        assertThatRuntimeException()
                .isThrownBy(() -> userService.login(command))
                .withMessage("Credenciales inválidas");

        verify(accountLockService).checkLockStatus(defaultUserId);
        verify(accountLockService).handleFailedLogin(defaultUserId);
        verify(accountLockService, never()).handleSuccessfulLogin(any());
    }

    @Test
    @DisplayName("Login con password incorrecta cuando se alcanza el límite propaga AccountLockedException")
    void shouldPropagateAccountLockedExceptionWhenBlockedOnFailedAttempt() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(defaultUserId);
        when(userRepository.findByIdentifier(email)).thenReturn(Optional.of(user));

        // Simulamos que handleFailedLogin lanza AccountLockedException (5º fallo)
        doThrow(new AccountLockedException(LocalDateTime.now().plusMinutes(15), 15))
                .when(accountLockService).handleFailedLogin(defaultUserId);

        LoginUserUseCase.LoginUserCommand command = new LoginUserUseCase.LoginUserCommand(email, "wrong");

        assertThatThrownBy(() -> userService.login(command))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("Inténtalo de nuevo en 15 minutos");

        verify(accountLockService).checkLockStatus(defaultUserId);
        verify(accountLockService).handleFailedLogin(defaultUserId);
        verify(passwordHasher, never()).matches(anyString(), anyString());
        verify(tokenProvider, never()).generateToken(any());
        verify(accountLockService, never()).handleSuccessfulLogin(any());
    }

    @Test
    @DisplayName("Login con usuario inexistente lanza error genérico sin comprobar bloqueos")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByIdentifier(email)).thenReturn(Optional.empty());

        LoginUserUseCase.LoginUserCommand command = new LoginUserUseCase.LoginUserCommand(email, rawPassword);

        assertThatRuntimeException()
                .isThrownBy(() -> userService.login(command))
                .withMessage("Credenciales inválidas");

        verify(accountLockService, never()).checkLockStatus(any());
        verify(accountLockService, never()).handleFailedLogin(any());
    }

    @Test
    @DisplayName("Login con cuenta bloqueada lanza AccountLockedException sin validar password")
    void shouldThrowWhenAccountIsLocked() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(defaultUserId);
        when(userRepository.findByIdentifier(email)).thenReturn(Optional.of(user));

        doThrow(new AccountLockedException(LocalDateTime.now().plusMinutes(15), 15))
                .when(accountLockService).checkLockStatus(defaultUserId);

        LoginUserUseCase.LoginUserCommand command = new LoginUserUseCase.LoginUserCommand(email, rawPassword);

        assertThatThrownBy(() -> userService.login(command))
                .isInstanceOf(AccountLockedException.class);

        verify(accountLockService).checkLockStatus(defaultUserId);
        verify(passwordHasher, never()).matches(anyString(), anyString());
        verify(accountLockService, never()).handleFailedLogin(any());
    }

    @Test
    @DisplayName("Obtener usuario por ID")
    void shouldGetUserById() {
        UUID id = UUID.randomUUID();
        User user = mock(User.class);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User result = userService.getUserById(id);

        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("Obtener usuario por ID inexistente lanza error")
    void shouldThrowWhenUserIdNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatRuntimeException()
                .isThrownBy(() -> userService.getUserById(id))
                .withMessage("Usuario no encontrado con el ID proporcionado");
    }
}