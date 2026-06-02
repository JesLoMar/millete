package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.family.domain.model.DistributionMode;
import com.puntomartinez.millete.family.domain.model.FamilyUnit;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity.FamilyUnitEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FamilyUnitEntityMapper {

    @Mapping(target = "distributionMode", source = "distributionMode", qualifiedByName = "mapDistributionModeToString")
    FamilyUnitEntity toEntity(FamilyUnit domain);

    @Mapping(target = "distributionMode", source = "distributionMode", qualifiedByName = "mapStringToDistributionMode")
    FamilyUnit toDomain(FamilyUnitEntity entity);

    @Named("mapDistributionModeToString")
    default String mapDistributionModeToString(DistributionMode mode) {
        return mode != null ? mode.name() : null;
    }

    @Named("mapStringToDistributionMode")
    default DistributionMode mapStringToDistributionMode(String mode) {
        return mode != null ? DistributionMode.valueOf(mode) : null;
    }
}