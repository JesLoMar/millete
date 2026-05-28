package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity.FamilyInvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface JpaFamilyInvitationRepository extends JpaRepository<FamilyInvitationEntity, UUID> {
    Optional<FamilyInvitationEntity> findByToken(String token);
    Optional<FamilyInvitationEntity> findByFamilyIdAndEmailAndStatus(UUID familyId, String email, String status);
}