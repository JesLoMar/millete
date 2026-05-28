package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.family.domain.model.FamilyContribution;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity.FamilyContributionEntity;
import org.springframework.stereotype.Component;

@Component
public class FamilyContributionMapper {

    public FamilyContributionEntity toEntity(FamilyContribution domain) {
        if (domain == null) return null;
        FamilyContributionEntity entity = new FamilyContributionEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setAmount(domain.getAmount());
        entity.setDate(domain.getDate());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setModifiedAt(domain.getModifiedAt());
        entity.setActive(domain.isActive());
        return entity;
    }

    public FamilyContribution toDomain(FamilyContributionEntity entity) {
        if (entity == null) return null;
        FamilyContribution domain = new FamilyContribution();
        domain.setId(entity.getId());
        domain.setFamilyId(entity.getFamilyId());
        domain.setUserId(entity.getUserId());
        domain.setAmount(entity.getAmount());
        domain.setDate(entity.getDate());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setModifiedAt(entity.getModifiedAt());
        domain.setActive(entity.isActive());
        return domain;
    }
}