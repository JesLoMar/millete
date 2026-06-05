package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.family.domain.model.FamilyContribution;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity.FamilyContributionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FamilyContributionEntityMapper {
    FamilyContributionEntity toEntity(FamilyContribution domain);
    FamilyContribution toDomain(FamilyContributionEntity entity);
}
