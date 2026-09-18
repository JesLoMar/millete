package com.puntomartinez.millete.users.infrastructure.in.controller;

import com.puntomartinez.millete.shared.infrastructure.config.CookieAuthFactory;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.ports.in.AuthenticateUserCommand;
import com.puntomartinez.millete.users.domain.ports.in.AuthenticateUserUseCase;
import com.puntomartinez.millete.users.domain.ports.in.AuthenticationResult;
import com.puntomartinez.millete.users.domain.ports.in.GetUserDataUseCase;
import com.puntomartinez.millete.users.domain.ports.in.ManageUserSessionUseCase;
import com.puntomartinez.millete.users.domain.ports.in.RegisterUserUseCase;
import com.puntomartinez.millete.users.infrastructure.in.controller.dto.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final GetUserDataUseCase getUserDataUseCase;
    private final ManageUserSessionUseCase manageUserSessionUseCase;
    private final CookieAuthFactory cookieAuthFactory;

    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            AuthenticateUserUseCase authenticateUserUseCase,
            GetUserDataUseCase getUserDataUseCase,
            ManageUserSessionUseCase manageUserSessionUseCase,
            CookieAuthFactory cookieAuthFactory
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.getUserDataUseCase = getUserDataUseCase;
        this.manageUserSessionUseCase = manageUserSessionUseCase;
        this.cookieAuthFactory = cookieAuthFactory;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(
            @Valid @RequestBody RegisterUserRequestDTO request
    ) {
        RegisterUserUseCase.RegisterUserCommand command =
                new RegisterUserUseCase.RegisterUserCommand(
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
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletResponse response
    ) {
        AuthenticateUserCommand command =
                new AuthenticateUserCommand(
                        request.identifier(),
                        request.password()
                );

        AuthenticationResult result =
                authenticateUserUseCase.authenticate(command);

        ResponseCookie cookie =
                cookieAuthFactory.createJwtCookie(result.jwt());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        Map<String, String> body = new HashMap<>();
        body.put("status", "authenticated");

        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(
            @RequestAttribute("sessionId") UUID sessionId,
            HttpServletResponse response
    ) {
        manageUserSessionUseCase.markSessionAsInactive(sessionId);

        ResponseCookie expiredCookie = cookieAuthFactory.createExpiredCookie();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me/topnav")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TopNavUserResponseDTO> getTopNavUserInfo(
            Authentication authentication,
            @RequestAttribute("sessionId") UUID sessionId
    ) {
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
}