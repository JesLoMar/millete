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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
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

    private final String cookieName;
    private final boolean cookieSecure;
    private final boolean cookieHttpOnly;
    private final String cookieSameSite;
    private final String cookiePath;
    private final long cookieMaxAge;

    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            LoginUserUseCase loginUserUseCase,
            GetUserDataUseCase getUserDataUseCase,
            UserService userService,
            TokenProvider tokenProvider,
            SessionPersistenceService sessionPersistenceService,
            @Value("${jwt.cookie-name}") String cookieName,
            @Value("${jwt.cookie-secure}") boolean cookieSecure,
            @Value("${jwt.cookie-http-only}") boolean cookieHttpOnly,
            @Value("${jwt.cookie-same-site}") String cookieSameSite,
            @Value("${jwt.cookie-path}") String cookiePath,
            @Value("${jwt.cookie-max-age}") long cookieMaxAge) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.getUserDataUseCase = getUserDataUseCase;
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.sessionPersistenceService = sessionPersistenceService;
        this.cookieName = cookieName;
        this.cookieSecure = cookieSecure;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieSameSite = cookieSameSite;
        this.cookiePath = cookiePath;
        this.cookieMaxAge = cookieMaxAge;
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
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDTO request,
                                                     HttpServletResponse response) {
        LoginUserUseCase.LoginUserCommand command = new LoginUserUseCase.LoginUserCommand(
                request.identifier(),
                request.password()
        );
        User user = loginUserUseCase.login(command);

        UserSession session = sessionPersistenceService.createSession(user.getId(), SessionPersistenceService.CHANNEL_WEB);
        String jwt = tokenProvider.generateToken(user, session.getId());

        ResponseCookie cookie = ResponseCookie.from(cookieName, jwt)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path(cookiePath)
                .maxAge(cookieMaxAge)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        Map<String, String> body = new HashMap<>();
        body.put("status", "authenticated");
        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(@RequestAttribute("sessionId") UUID sessionId,
                                       HttpServletResponse response) {
        sessionPersistenceService.markSessionAsInactive(sessionId);

        ResponseCookie expiredCookie = ResponseCookie.from(cookieName, "")
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path(cookiePath)
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me/topnav")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TopNavUserResponseDTO> getTopNavUserInfo(Authentication authentication,
                                                                   @RequestAttribute("sessionId") UUID sessionId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        User user = getUserDataUseCase.getUserById(jwtUser.getId());
        TopNavUserResponseDTO response = new TopNavUserResponseDTO(
                user.getUsername(),
                user.getEmail(),
                sessionId
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getTelegramStatus(
            @RequestParam Long chatId,
            Authentication authentication) {

        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        User user = userService.getUserById(jwtUser.getId());

        Map<String, Object> response = new HashMap<>();
        boolean linked = user.getTelegramChatId() != null && user.getTelegramChatId().equals(chatId);
        response.put("linked", linked);

        return ResponseEntity.ok(response);
    }
}
