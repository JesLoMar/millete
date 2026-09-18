package com.puntomartinez.millete.users.infrastructure.in.controller;

import com.puntomartinez.millete.shared.infrastructure.config.CookieAuthFactory;
import com.puntomartinez.millete.shared.infrastructure.config.filter.LoginRateLimitFilter;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.domain.ports.in.GetUserDataUseCase;
import com.puntomartinez.millete.users.domain.ports.in.LoginUserUseCase;
import com.puntomartinez.millete.users.domain.ports.in.ManageUserSessionUseCase;
import com.puntomartinez.millete.users.domain.ports.in.RegisterUserUseCase;
import com.puntomartinez.millete.users.domain.ports.out.TokenProvider;
import com.puntomartinez.millete.users.infrastructure.in.controller.dto.*;
import jakarta.servlet.http.HttpServletRequest;
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
    private final LoginUserUseCase loginUserUseCase;
    private final GetUserDataUseCase getUserDataUseCase;
    private final TokenProvider tokenProvider;
    private final ManageUserSessionUseCase manageUserSessionUseCase;
    private final CookieAuthFactory cookieAuthFactory;
    private final LoginRateLimitFilter loginRateLimitFilter;

    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            LoginUserUseCase loginUserUseCase,
            GetUserDataUseCase getUserDataUseCase,
            TokenProvider tokenProvider,
            ManageUserSessionUseCase manageUserSessionUseCase,
            CookieAuthFactory cookieAuthFactory,
            LoginRateLimitFilter loginRateLimitFilter
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.getUserDataUseCase = getUserDataUseCase;
        this.tokenProvider = tokenProvider;
        this.manageUserSessionUseCase = manageUserSessionUseCase;
        this.cookieAuthFactory = cookieAuthFactory;
        this.loginRateLimitFilter = loginRateLimitFilter;
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
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        LoginUserUseCase.LoginUserCommand command =
                new LoginUserUseCase.LoginUserCommand(
                        request.identifier(),
                        request.password()
                );

        User user = loginUserUseCase.login(command);

        // Resetear el rate limit tras login exitoso (Bug 11)
        String clientIp = LoginRateLimitFilter.resolveClientIp(httpRequest);
        loginRateLimitFilter.resetForIp(clientIp);

        UserSession session = manageUserSessionUseCase.createSession(
                user.getId(),
                UserSession.CHANNEL_WEB
        );

        String jwt = tokenProvider.generateToken(user, session.getId());

        ResponseCookie cookie = cookieAuthFactory.createJwtCookie(jwt);
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