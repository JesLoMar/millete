package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.ports.in.LoginUserUseCase;
import com.puntomartinez.millete.users.domain.ports.in.RegisterUserUseCase;
import com.puntomartinez.millete.users.domain.ports.out.PasswordHasherPort;
import com.puntomartinez.millete.users.domain.ports.out.TokenProvider;
import com.puntomartinez.millete.users.domain.ports.out.UserRepository;
import com.puntomartinez.millete.users.domain.ports.in.GetUserDataUseCase;
import com.puntomartinez.millete.shared.domain.exception.AuthenticationFailedException;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceAlreadyExistsException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService implements RegisterUserUseCase, LoginUserUseCase, GetUserDataUseCase {

    private final UserRepository userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenProvider tokenProvider;
    private final AccountLockService accountLockService;

    private final String dummyPasswordHash;

    public UserService(UserRepository userRepository,
                       PasswordHasherPort passwordHasher,
                       TokenProvider tokenProvider,
                       AccountLockService accountLockService) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.accountLockService = accountLockService;
        this.dummyPasswordHash = passwordHasher.hashPassword(UUID.randomUUID().toString());
    }

    @Override
    public User register(RegisterUserCommand command) {
        boolean hasUsername = command.username() != null && !command.username().isBlank();
        boolean hasEmail = command.email() != null && !command.email().isBlank();
        if (!hasUsername && !hasEmail) {
            throw new InvalidInputException("Se requiere un email o un nombre de usuario para registrarse.");
        }
        if (hasEmail && userRepository.findByEmail(command.email()).isPresent()) {
            throw new ResourceAlreadyExistsException("El usuario o el email ya están registrados.");
        }
        if (hasUsername && userRepository.findByUsername(command.username()).isPresent()) {
            throw new ResourceAlreadyExistsException("El usuario o el email ya están registrados.");
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

    @Override
    public User login(LoginUserCommand command) {
        var maybeUser = userRepository.findByIdentifier(command.identifier());

        if (maybeUser.isEmpty()) {
            passwordHasher.matches(command.rawPassword(), dummyPasswordHash);
            throw new AuthenticationFailedException("Credenciales inválidas");
        }

        User user = maybeUser.get();
        accountLockService.checkLockStatus(user.getId());
        if (!passwordHasher.matches(command.rawPassword(), user.getPassword())) {
            accountLockService.handleFailedLogin(user.getId());
            throw new AuthenticationFailedException("Credenciales inválidas");
        }
        accountLockService.handleSuccessfulLogin(user.getId());
        return user;
    }

    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el ID proporcionado"));
    }

    public void linkTelegram(UUID userId, Long chatId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        userRepository.findByTelegramChatId(chatId).ifPresent(existing -> {
            if (!existing.getId().equals(userId)) {
                throw new ResourceAlreadyExistsException("Este Telegram ya está vinculado a otra cuenta");
            }
        });
        user.setTelegramChatId(chatId);
        user.setModifiedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public UUID getUserIdByTelegramChatId(Long chatId) {
        return userRepository.findByTelegramChatId(chatId)
                .map(User::getId)
                .orElse(null);
    }
}