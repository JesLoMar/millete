package com.puntomartinez.millete.users.infrastructure.in.controller;

import com.puntomartinez.millete.users.application.services.SessionPersistenceService;
import com.puntomartinez.millete.users.application.services.UserService;
import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.domain.ports.in.LoginUserUseCase;
import com.puntomartinez.millete.users.domain.ports.in.RegisterUserUseCase;
import com.puntomartinez.millete.users.domain.ports.in.GetUserDataUseCase;
import com.puntomartinez.millete.users.domain.ports.out.TokenProvider;
import com.puntomartinez.millete.users.infrastructure.in.controller.dto.*;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final GetUserDataUseCase getUserDataUseCase;
    private final UserService userService;
    private final TokenProvider tokenProvider;
    private final SessionPersistenceService sessionPersistenceService;

    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            LoginUserUseCase loginUserUseCase,
            GetUserDataUseCase getUserDataUseCase,
            UserService userService,
            TokenProvider tokenProvider,
            SessionPersistenceService sessionPersistenceService) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.getUserDataUseCase = getUserDataUseCase;
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.sessionPersistenceService = sessionPersistenceService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterUserRequestDTO request) {
        RegisterUserUseCase.RegisterUserCommand command = new RegisterUserUseCase.RegisterUserCommand(
                request.username(),
                request.email(),
                request.password()
        );
        User user = registerUserUseCase.register(command);
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
        User user = loginUserUseCase.login(command);

        UserSession session = sessionPersistenceService.createSession(user.getId(), SessionPersistenceService.CHANNEL_WEB);
        String jwt = tokenProvider.generateToken(user, session.getId());
        return ResponseEntity.ok(new TokenResponseDTO(jwt));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(@RequestAttribute("sessionId") UUID sessionId) {
        sessionPersistenceService.markSessionAsInactive(sessionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me/topnav")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TopNavUserResponseDTO> getTopNavUserInfo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        User user = getUserDataUseCase.getUserById(jwtUser.getId());
        TopNavUserResponseDTO response = new TopNavUserResponseDTO(
                user.getUsername(),
                user.getEmail()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/telegram/link")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> linkTelegram(
            @RequestBody Map<String, Long> request,
            Authentication authentication) {

        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        Long chatId = request.get("chatId");

        if (chatId == null) {
            return ResponseEntity.badRequest().build();
        }

        userService.linkTelegram(jwtUser.getId(), chatId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/telegram/status")
    public ResponseEntity<Map<String, Object>> getTelegramStatus(
            @RequestParam Long chatId) {

        UUID userId = userService.getUserIdByTelegramChatId(chatId);

        Map<String, Object> response = new HashMap<>();
        response.put("linked", userId != null);
        if (userId != null) {
            response.put("userId", userId.toString());
        }

        return ResponseEntity.ok(response);
    }
}
