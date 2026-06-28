package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.users.domain.model.UserPreferences;
import com.puntomartinez.millete.users.domain.ports.out.UserPreferencesRepository;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserPreferencesEntity;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers.UserPreferencesEntityMapper;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.repository.JpaUserPreferencesRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserPreferencesPostgresAdapter implements UserPreferencesRepository {

    private final JpaUserPreferencesRepository repository;
    private final UserPreferencesEntityMapper mapper;

    public UserPreferencesPostgresAdapter(JpaUserPreferencesRepository repository, UserPreferencesEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<UserPreferences> findByUserId(UUID userId) {
        return repository.findByUserId(userId)
                .map(mapper::toDomain);
    }

    @Override
    public UserPreferences save(UserPreferences preferences) {
        UserPreferencesEntity entity = mapper.toEntity(preferences);
        UserPreferencesEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        repository.deleteByUserId(userId);
    }
}
