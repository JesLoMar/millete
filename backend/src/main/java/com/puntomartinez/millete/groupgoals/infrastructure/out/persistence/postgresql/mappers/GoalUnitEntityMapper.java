package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.groupgoals.domain.model.DistributionMode;
import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalUnitEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GoalUnitEntityMapper {

    GoalUnitEntity toEntity(GoalUnit goalUnit);

    default GoalUnit toDomain(GoalUnitEntity entity) {
        return GoalUnit.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getMonthlyTarget(),
                DistributionMode.valueOf(entity.getDistributionMode()),
                entity.getCreatedAt(),
                entity.getModifiedAt(),
                entity.isActive()
        );
    }
}