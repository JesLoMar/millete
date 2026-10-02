package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetRepository;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.UserAssetEntity;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.UserAssetEntityMapper;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository.JpaUserAssetRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public final class UserAssetPostgresAdapter
        implements UserAssetRepository {

    private final JpaUserAssetRepository repository;
    private final UserAssetEntityMapper mapper;

    public UserAssetPostgresAdapter(
            JpaUserAssetRepository repository,
            UserAssetEntityMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public UserAsset save(
            UserAsset userAsset
    ) {
        if (userAsset == null) {
            throw new IllegalArgumentException(
                    "El UserAsset es obligatorio."
            );
        }

        UserAssetEntity entity = mapper.toEntity(userAsset);
        UserAssetEntity savedEntity = repository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<UserAsset> findByIdAndUserId(
            UUID userAssetId,
            UUID userId
    ) {
        if (userAssetId == null || userId == null) {
            return Optional.empty();
        }

        return repository.findByIdAndUserId(
                        userAssetId,
                        userId
                )
                .map(mapper::toDomain);
    }

    @Override
    public List<UserAsset> findAllByUserId(
            UUID userId,
            String search
    ) {
        if (userId == null) {
            return List.of();
        }

        List<UserAssetEntity> entities;

        if (search == null || search.isBlank()) {
            entities = repository.findByUserIdOrderByNameAscIdAsc(userId);
        } else {
            entities = repository
                    .findByUserIdAndNameContainingIgnoreCaseOrderByNameAscIdAsc(
                            userId,
                            search.trim()
                    );
        }

        return entities.stream()
                .map(mapper::toDomain)
                .toList();
    }
}