package com.puntomartinez.millete.users.domain.ports.out;

import com.puntomartinez.millete.users.domain.model.User;

public interface TokenProvider {
    String generateToken(User user);
    String extractUserId(String token);
    boolean isTokenValid(String token);
}