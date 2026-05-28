package com.puntomartinez.millete.users.domain.ports.out;
import com.puntomartinez.millete.users.domain.model.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByIdentifier(String identifier);
    Optional<User> findById(UUID id);
}