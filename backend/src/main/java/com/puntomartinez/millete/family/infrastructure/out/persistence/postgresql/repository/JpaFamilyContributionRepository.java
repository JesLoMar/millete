package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity.FamilyContributionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaFamilyContributionRepository extends JpaRepository<FamilyContributionEntity, UUID> {
    List<FamilyContributionEntity> findByFamilyIdAndActiveTrueOrderByDateDesc(UUID familyId);
}