package com.puntomartinez.millete.users.infrastructure.in.controller;

import com.puntomartinez.millete.shared.infrastructure.config.CookieAuthFactory;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.users.domain.ports.in.ManageProfileUseCase;
import com.puntomartinez.millete.users.infrastructure.in.controller.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final ManageProfileUseCase profileUseCase;
    private final CookieAuthFactory cookieAuthFactory;

    public ProfileController(ManageProfileUseCase profileUseCase, CookieAuthFactory cookieAuthFactory) {
        this.profileUseCase = profileUseCase;
        this.cookieAuthFactory = cookieAuthFactory;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ManageProfileUseCase.UserProfileDTO> getProfile(Authentication authentication) {
        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        return ResponseEntity.ok(profileUseCase.getProfile(jwtUser.getId()));
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateProfile(
            Authentication authentication,
            @RequestBody @Valid UpdateProfileRequestDTO request) {
        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        ManageProfileUseCase.UpdateProfileCommand command = new ManageProfileUseCase.UpdateProfileCommand(
                request.newUsername(), request.newEmail(), request.currentPassword()
        );
        profileUseCase.updateProfile(jwtUser.getId(), command);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @RequestAttribute("sessionId") UUID sessionId,
            @RequestBody @Valid ChangePasswordRequestDTO request) {
        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        ManageProfileUseCase.ChangePasswordCommand command = new ManageProfileUseCase.ChangePasswordCommand(
                request.currentPassword(), request.newPassword(), sessionId
        );
        profileUseCase.changePassword(jwtUser.getId(), command);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/preferences")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> getPreferences(Authentication authentication) {
        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        return ResponseEntity.ok(profileUseCase.getPreferences(jwtUser.getId()));
    }

    @PutMapping("/preferences")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updatePreferences(
            Authentication authentication,
            @RequestBody String preferencesJson) {
        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        profileUseCase.updatePreferences(jwtUser.getId(), preferencesJson);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserSessionResponseDTO>> getActiveSessions(Authentication authentication) {
        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        List<UserSessionResponseDTO> sessions = profileUseCase.getActiveSessions(jwtUser.getId()).stream()
                .map(s -> new UserSessionResponseDTO(s.getId(), s.getChannel(), s.isActive(), s.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/sessions/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> closeSession(
            Authentication authentication,
            @PathVariable UUID sessionId) {
        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        profileUseCase.closeSession(jwtUser.getId(), sessionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> closeAllOtherSessions(
            Authentication authentication,
            @RequestAttribute("sessionId") UUID currentSessionId) {
        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        profileUseCase.closeAllOtherSessions(jwtUser.getId(), currentSessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deactivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deactivateAccount(
            Authentication authentication,
            @RequestBody @Valid DeactivateAccountRequestDTO request) {
        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        profileUseCase.deactivateAccount(jwtUser.getId(), request.password());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieAuthFactory.createExpiredCookie().toString())
                .build();
    }
}