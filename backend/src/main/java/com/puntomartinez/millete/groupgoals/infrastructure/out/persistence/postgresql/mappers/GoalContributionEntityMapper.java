package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalContributionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GoalContributionEntityMapper {
    GoalContributionEntity toEntity(GoalContribution domain);
    GoalContribution toDomain(GoalContributionEntity entity);
}