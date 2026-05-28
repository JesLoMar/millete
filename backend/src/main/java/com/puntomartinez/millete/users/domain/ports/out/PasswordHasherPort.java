package com.puntomartinez.millete.users.domain.ports.out;

public interface PasswordHasherPort {
    String hashPassword(String rawPassword);
    boolean matches(String rawPassword, String hashedPassword);
}