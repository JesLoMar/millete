package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.domain.ports.out.UserSessionRepository;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserSessionEntity;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers.UserSessionEntityMapper;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.repository.SpringDataUserSessionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserSessionPostgresAdapter implements UserSessionRepository {

    private final SpringDataUserSessionRepository repository;
    private final UserSessionEntityMapper mapper;

    public UserSessionPostgresAdapter(SpringDataUserSessionRepository repository, UserSessionEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<UserSession> findByUserIdAndChannel(UUID userId, String channel) {
        return repository.findByUserIdAndChannel(userId, channel).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<UserSession> findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public UserSession save(UserSession session) {
        UserSessionEntity entity = mapper.toEntity(session);
        UserSessionEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByIdAndActiveTrue(UUID id) {
        return repository.existsByIdAndActiveTrue(id);
    }

    @Override
    public List<UserSession> findByUserIdAndActiveTrue(UUID userId) {
        return repository.findByUserIdAndActiveTrue(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deactivateAllOtherSessions(UUID userId, UUID currentSessionId) {
        repository.deactivateAllOtherSessions(userId, currentSessionId);
    }

    @Override
    public void deactivateAllSessions(UUID userId) {
        repository.deactivateAllSessions(userId);
    }
}
