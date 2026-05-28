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

    // Inyectamos todas las dependencias necesarias
    public UserService(UserRepository userRepository,
                       PasswordHasherPort passwordHasher,
                       TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    // ==========================================
    // CASO DE USO: REGISTRAR USUARIO
    // ==========================================
    @Override
    public User register(RegisterUserCommand command) {
        boolean hasUsername = command.username() != null && !command.username().isBlank();
        boolean hasEmail = command.email() != null && !command.email().isBlank();

        // 1. Validar que venga al menos un identificador
        if (!hasUsername && !hasEmail) {
            throw new RuntimeException("Se requiere un email o un nombre de usuario para registrarse.");
        }

        // 2. Verificamos que email y username estén libres (solo si se han enviado)
        if (hasEmail && userRepository.findByEmail(command.email()).isPresent()) {
            throw new RuntimeException("El email " + command.email() + " ya está registrado.");
        }
        if (hasUsername && userRepository.findByUsername(command.username()).isPresent()) {
            throw new RuntimeException("El nombre de usuario " + command.username() + " ya está en uso.");
        }

        // 3. Encriptamos
        String encryptedPassword = passwordHasher.hashPassword(command.rawPassword());
        LocalDateTime now = LocalDateTime.now();

        // 4. Creamos el modelo (los nulos se pasarán directamente si el usuario no los envió)
        User newUser = new User(
                UUID.randomUUID(),
                hasUsername ? command.username() : null,
                hasEmail ? command.email() : null,
                encryptedPassword,
                now,         // createdAt
                now,         // modifiedAt (igual al crearlo)
                true,        // active
                false        // isAnonymized
        );

        // 5. Guardamos
        return userRepository.save(newUser);
    }

    // ==========================================
    // CASO DE USO: LOGIN
    // ==========================================
    @Override
    public String login(LoginUserCommand command) {
        // 1. Buscamos al usuario por su identificador (puede ser email o username)
        User user = userRepository.findByIdentifier(command.identifier())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
        // Usamos un mensaje genérico por seguridad (no dar pistas a atacantes)

        // 2. Comprobamos si la contraseña coincide (BCrypt hace la magia)
        if (!passwordHasher.matches(command.rawPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }
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