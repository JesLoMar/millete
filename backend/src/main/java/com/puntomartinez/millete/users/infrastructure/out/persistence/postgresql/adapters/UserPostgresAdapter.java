package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.ports.out.UserRepository;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserEntity;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers.UserEntityMapper;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.repository.JpaUserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserPostgresAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final UserEntityMapper mapper;

    // Inyección de dependencias
    public UserPostgresAdapter(JpaUserRepository jpaUserRepository, UserEntityMapper mapper) {
        this.jpaUserRepository = jpaUserRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        // 1. Traducimos al idioma de base de datos
        UserEntity entity = mapper.toEntity(user);

        // 2. Guardamos (Hibernate hace el INSERT)
        UserEntity savedEntity = jpaUserRepository.save(entity);

        // 3. Devolvemos el modelo de dominio puro
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email)
                .map(mapper::toDomain); // Si lo encuentra, lo mapea a Dominio
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaUserRepository.findByUsername(username)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByIdentifier(String identifier) {
        return jpaUserRepository.findByUsernameOrEmail(identifier, identifier)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id)
                .map(mapper::toDomain);
    }
}