package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.family.domain.model.*;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity.*;
import org.springframework.stereotype.Component;

@Component
public class FamilyPersistenceMapper {

    // Ejemplo para FamilyUnit (puedes completar los demás siguiendo este patrón)
    public FamilyUnitEntity toEntity(FamilyUnit domain) {
        if (domain == null) return null;
        FamilyUnitEntity entity = new FamilyUnitEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setMonthlyTarget(domain.getMonthlyTarget());
        entity.setDistributionMode(domain.getDistributionMode().name());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setModifiedAt(domain.getModifiedAt());
        entity.setActive(domain.isActive());
        return entity;
    }

    public FamilyUnit toDomain(FamilyUnitEntity entity) {
        if (entity == null) return null;
        FamilyUnit domain = new FamilyUnit();
        domain.setId(entity.getId());
        domain.setName(entity.getName());
        domain.setMonthlyTarget(entity.getMonthlyTarget());
        domain.setDistributionMode(DistributionMode.valueOf(entity.getDistributionMode()));
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setModifiedAt(entity.getModifiedAt());
        domain.setActive(entity.isActive());
        return domain;
    }
}