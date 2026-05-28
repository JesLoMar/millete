package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.family.domain.model.FamilyInvitation;
import com.puntomartinez.millete.family.domain.model.InvitationStatus;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity.FamilyInvitationEntity;
import org.springframework.stereotype.Component;

@Component
public class FamilyInvitationEntityMapper {

    public FamilyInvitationEntity toEntity(FamilyInvitation domain) {
        if (domain == null) return null;

        FamilyInvitationEntity entity = new FamilyInvitationEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setEmail(domain.getEmail());
        entity.setToken(domain.getToken());
        entity.setStatus(domain.getStatus().name());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setModifiedAt(domain.getModifiedAt());
        entity.setActive(domain.isActive());

        return entity;
    }

    public FamilyInvitation toDomain(FamilyInvitationEntity entity) {
        if (entity == null) return null;

        FamilyInvitation domain = new FamilyInvitation();
        domain.setId(entity.getId());
        domain.setFamilyId(entity.getFamilyId());
        domain.setEmail(entity.getEmail());
        domain.setToken(entity.getToken());
        domain.setStatus(InvitationStatus.valueOf(entity.getStatus()));
        domain.setExpiresAt(entity.getExpiresAt());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setModifiedAt(entity.getModifiedAt());
        domain.setActive(entity.isActive());

        return domain;
    }
}