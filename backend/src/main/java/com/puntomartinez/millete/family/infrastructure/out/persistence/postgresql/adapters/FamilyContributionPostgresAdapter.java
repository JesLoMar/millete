package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.family.domain.model.FamilyContribution;
import com.puntomartinez.millete.family.domain.ports.out.FamilyContributionRepository;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity.FamilyContributionEntity;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.mappers.FamilyContributionEntityMapper;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.repository.JpaFamilyContributionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class FamilyContributionPostgresAdapter implements FamilyContributionRepository {

    private final JpaFamilyContributionRepository jpaRepository;
    private final FamilyContributionEntityMapper mapper;

    public FamilyContributionPostgresAdapter(JpaFamilyContributionRepository jpaRepository, FamilyContributionEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public FamilyContribution save(FamilyContribution contribution) {
        FamilyContributionEntity entity = mapper.toEntity(contribution);
        FamilyContributionEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<FamilyContribution> findByFamilyId(UUID familyId) {
        return jpaRepository.findByFamilyIdAndActiveTrueOrderByDateDesc(familyId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}