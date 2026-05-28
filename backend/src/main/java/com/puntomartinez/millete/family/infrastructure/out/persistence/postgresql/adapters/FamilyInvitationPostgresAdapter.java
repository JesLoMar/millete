package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.family.domain.model.FamilyInvitation;
import com.puntomartinez.millete.family.domain.model.InvitationStatus;
import com.puntomartinez.millete.family.domain.ports.out.FamilyInvitationRepository;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity.FamilyInvitationEntity;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.mappers.FamilyInvitationEntityMapper;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.repository.JpaFamilyInvitationRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class FamilyInvitationPostgresAdapter implements FamilyInvitationRepository {

    private final JpaFamilyInvitationRepository jpaRepository;
    private final FamilyInvitationEntityMapper mapper;

    public FamilyInvitationPostgresAdapter(JpaFamilyInvitationRepository jpaRepository, FamilyInvitationEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public FamilyInvitation save(FamilyInvitation familyInvitation) {
        FamilyInvitationEntity entity = mapper.toEntity(familyInvitation);
        FamilyInvitationEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<FamilyInvitation> findByToken(String token) {
        return jpaRepository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public Optional<FamilyInvitation> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<FamilyInvitation> findByFamilyIdAndEmailAndStatus(UUID familyId, String email, InvitationStatus status) {
        return jpaRepository.findByFamilyIdAndEmailAndStatus(familyId, email, status.name())
                .map(mapper::toDomain);
    }
}