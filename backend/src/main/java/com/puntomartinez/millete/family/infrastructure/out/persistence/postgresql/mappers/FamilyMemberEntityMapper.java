package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.family.domain.model.FamilyMember;
import com.puntomartinez.millete.family.domain.model.FamilyRole;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity.FamilyMemberEntity;
import org.springframework.stereotype.Component;

@Component
public class FamilyMemberEntityMapper {

    public FamilyMemberEntity toEntity(FamilyMember domain) {
        if (domain == null) return null;

        FamilyMemberEntity entity = new FamilyMemberEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setRole(domain.getRole().name());
        entity.setSalary(domain.getSalary());
        entity.setCustomPercentage(domain.getCustomPercentage());
        entity.setJoinedAt(domain.getJoinedAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setModifiedAt(domain.getModifiedAt());
        entity.setActive(domain.isActive());

        return entity;
    }

    public FamilyMember toDomain(FamilyMemberEntity entity) {
        if (entity == null) return null;

        FamilyMember domain = new FamilyMember();
        domain.setId(entity.getId());
        domain.setFamilyId(entity.getFamilyId());
        domain.setUserId(entity.getUserId());
        domain.setRole(FamilyRole.valueOf(entity.getRole()));
        domain.setSalary(entity.getSalary());
        domain.setCustomPercentage(entity.getCustomPercentage());
        domain.setJoinedAt(entity.getJoinedAt());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setModifiedAt(entity.getModifiedAt());
        domain.setActive(entity.isActive());

        return domain;
    }
}