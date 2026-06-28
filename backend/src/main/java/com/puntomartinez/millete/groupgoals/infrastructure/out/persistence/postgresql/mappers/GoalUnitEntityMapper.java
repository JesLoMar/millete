package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.groupgoals.domain.model.DistributionMode;
import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalUnitEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GoalUnitEntityMapper {

    @Mapping(target = "distributionMode", expression = "java(mapDistributionModeToString(domain.getDistributionMode()))")
    GoalUnitEntity toEntity(GoalUnit domain);

    @Mapping(target = "distributionMode", expression = "java(mapStringToDistributionMode(entity.getDistributionMode()))")
    GoalUnit toDomain(GoalUnitEntity entity);

    default String mapDistributionModeToString(DistributionMode mode) {
        return mode != null ? mode.name() : null;
    }

    default DistributionMode mapStringToDistributionMode(String mode) {
        return mode != null ? DistributionMode.valueOf(mode) : null;
    }
}