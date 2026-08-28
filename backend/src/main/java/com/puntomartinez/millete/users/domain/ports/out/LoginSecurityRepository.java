package com.puntomartinez.millete.users.domain.ports.out;
import com.puntomartinez.millete.users.domain.model.UserLoginSecurity;
import java.util.Optional;
import java.util.UUID;
public interface LoginSecurityRepository {
    Optional<UserLoginSecurity> findByUserId(UUID userId);
    UserLoginSecurity save(UserLoginSecurity security);
}