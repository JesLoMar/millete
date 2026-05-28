package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByUsernameOrEmail(String username, String email);
}