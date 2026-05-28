package com.puntomartinez.millete.users.infrastructure.in.controller;

import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.ports.in.LoginUserUseCase;
import com.puntomartinez.millete.users.domain.ports.in.RegisterUserUseCase;
import com.puntomartinez.millete.users.domain.ports.in.GetUserDataUseCase;
import com.puntomartinez.millete.users.infrastructure.in.controller.dto.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final GetUserDataUseCase getUserDataUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, LoginUserUseCase loginUserUseCase, GetUserDataUseCase getUserDataUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.getUserDataUseCase = getUserDataUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterUserRequestDTO request) {

        // 1. Traducimos el DTO al Comando de dominio
        RegisterUserUseCase.RegisterUserCommand command = new RegisterUserUseCase.RegisterUserCommand(
                request.username(),
                request.email(),
                request.password()
        );

        // 2. Ejecutamos la lógica de negocio
        User user = registerUserUseCase.register(command);

        // 3. Mapeamos el resultado al DTO de respuesta
        UserResponseDTO response = new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getModifiedAt(),
                user.isActive(),
                user.isAnonymized()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {

        LoginUserUseCase.LoginUserCommand command = new LoginUserUseCase.LoginUserCommand(
                request.identifier(),
                request.password()
        );

        String jwt = loginUserUseCase.login(command);

        return ResponseEntity.ok(new TokenResponseDTO(jwt));
    }

    @GetMapping("/me/topnav")
    public ResponseEntity<TopNavUserResponseDTO> getTopNavUserInfo(Authentication authentication) {
        String userIdString = authentication.getName();
        UUID userId = UUID.fromString(userIdString);
        User user = getUserDataUseCase.getUserById(userId);
        TopNavUserResponseDTO response = new TopNavUserResponseDTO(
                user.getUsername(),
                user.getEmail()
        );
        return ResponseEntity.ok(response);
    }
}