package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.groupgoals.domain.model.GoalMember;
import com.puntomartinez.millete.groupgoals.domain.model.GoalRole;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalMemberEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GoalMemberEntityMapper {

    GoalMemberEntity toEntity(GoalMember goalMember);

    default GoalMember toDomain(GoalMemberEntity entity) {
        return GoalMember.reconstitute(
                entity.getId(),
                entity.getGoalId(),
                entity.getUserId(),
                GoalRole.valueOf(entity.getRole()),
                entity.getSalary(),
                entity.getCustomPercentage(),
                entity.getJoinedAt(),
                entity.getCreatedAt(),
                entity.getModifiedAt(),
                entity.isActive()
        );
    }
}