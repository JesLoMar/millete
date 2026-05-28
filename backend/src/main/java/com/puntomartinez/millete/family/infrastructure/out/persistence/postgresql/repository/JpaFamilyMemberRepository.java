package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity.FamilyMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaFamilyMemberRepository extends JpaRepository<FamilyMemberEntity, UUID> {
    Optional<FamilyMemberEntity> findByFamilyIdAndUserId(UUID familyId, UUID userId);
    List<FamilyMemberEntity> findByFamilyIdAndActiveTrue(UUID familyId);
    List<FamilyMemberEntity> findByUserIdAndActiveTrue(UUID userId);
    void deleteByFamilyIdAndUserId(UUID familyId, UUID userId);
}