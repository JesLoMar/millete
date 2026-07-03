package com.puntomartinez.millete.users.domain.ports.out;

import com.puntomartinez.millete.users.domain.model.User;

import java.util.UUID;

public interface TokenProvider {
    String generateToken(User user);
    String generateToken(User user, UUID sessionId);
    String extractUserId(String token);
    boolean isTokenValid(String token);
    String getClaim(String token, String claimName);
}
