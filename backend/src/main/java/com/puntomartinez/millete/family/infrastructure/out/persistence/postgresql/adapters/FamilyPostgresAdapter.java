package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.family.domain.model.FamilyUnit;
import com.puntomartinez.millete.family.domain.ports.out.FamilyUnitRepository;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity.FamilyUnitEntity;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.mappers.FamilyPersistenceMapper;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.repository.JpaFamilyUnitRepository;

import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class FamilyPostgresAdapter implements FamilyUnitRepository {

    private final JpaFamilyUnitRepository jpaRepository;
    private final FamilyPersistenceMapper mapper;

    public FamilyPostgresAdapter(JpaFamilyUnitRepository jpaRepository, FamilyPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public FamilyUnit save(FamilyUnit familyUnit) {
        FamilyUnitEntity entity = mapper.toEntity(familyUnit);
        FamilyUnitEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<FamilyUnit> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}