package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.ports.in.LoginUserUseCase;
import com.puntomartinez.millete.users.domain.ports.in.RegisterUserUseCase;
import com.puntomartinez.millete.users.domain.ports.out.PasswordHasherPort;
import com.puntomartinez.millete.users.domain.ports.out.TokenProvider;
import com.puntomartinez.millete.users.domain.ports.out.UserRepository;
import com.puntomartinez.millete.users.domain.ports.in.GetUserDataUseCase;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService implements RegisterUserUseCase, LoginUserUseCase, GetUserDataUseCase {

    private final UserRepository userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenProvider tokenProvider;
    private final AccountLockService accountLockService;

    public UserService(UserRepository userRepository,
                       PasswordHasherPort passwordHasher,
                       TokenProvider tokenProvider,
                       AccountLockService accountLockService) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.accountLockService = accountLockService;
    }

    // ==========================================
    // CASO DE USO: REGISTRAR USUARIO
    // ==========================================
    @Override
    public User register(RegisterUserCommand command) {
        boolean hasUsername = command.username() != null && !command.username().isBlank();
        boolean hasEmail = command.email() != null && !command.email().isBlank();
        if (!hasUsername && !hasEmail) {
            throw new RuntimeException("Se requiere un email o un nombre de usuario para registrarse.");
        }
        if (hasEmail && userRepository.findByEmail(command.email()).isPresent()) {
            throw new RuntimeException("El email " + command.email() + " ya está registrado.");
        }
        if (hasUsername && userRepository.findByUsername(command.username()).isPresent()) {
            throw new RuntimeException("El nombre de usuario " + command.username() + " ya está en uso.");
        }
        String encryptedPassword = passwordHasher.hashPassword(command.rawPassword());
        LocalDateTime now = LocalDateTime.now();
        User newUser = new User(
                UUID.randomUUID(),
                hasUsername ? command.username() : null,
                hasEmail ? command.email() : null,
                encryptedPassword,
                now,
                now,
                true,
                false
        );
        return userRepository.save(newUser);
    }

    // ==========================================
    // CASO DE USO: LOGIN
    // ==========================================
    @Override
    public String login(LoginUserCommand command) {
        User user = userRepository.findByIdentifier(command.identifier())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        // 2. Control preliminar de bloqueo: ¿Este usuario tiene la sesión web temporalmente bloqueada?
        accountLockService.checkLockStatus(user.getId());
        if (!passwordHasher.matches(command.rawPassword(), user.getPassword())) {
            accountLockService.handleFailedLogin(user.getId());
            throw new RuntimeException("Credenciales inválidas");
        }
        accountLockService.handleSuccessfulLogin(user.getId());

        return tokenProvider.generateToken(user);
    }

    // ==========================================
    // RECUPERAR USUARIO POR ID
    // ==========================================
    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el ID proporcionado"));
    }
}