package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.groupgoals.domain.model.ContributionType;
import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalContributionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GoalContributionEntityMapper {

    @Mapping(
            target = "type",
            source = "type",
            qualifiedByName = "mapTypeToString"
    )
    GoalContributionEntity toEntity(GoalContribution domain);

    default GoalContribution toDomain(GoalContributionEntity entity) {
        if (entity == null) {
            return null;
        }
        return GoalContribution.reconstitute(
                entity.getId(),
                entity.getGoalId(),
                entity.getUserId(),
                entity.getAmount(),
                mapStringToType(entity.getType()),
                entity.getDate(),
                entity.getCreatedAt(),
                entity.getModifiedAt(),
                entity.isActive()
        );
    }

    @Named("mapTypeToString")
    default String mapTypeToString(ContributionType type) {
        return type != null ? type.name() : null;
    }

    default ContributionType mapStringToType(String type) {
        return type != null ? ContributionType.valueOf(type) : null;
    }
}