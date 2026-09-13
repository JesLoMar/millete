package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.groupgoals.domain.model.GoalInvitation;
import com.puntomartinez.millete.groupgoals.domain.model.InvitationStatus;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalInvitationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GoalInvitationEntityMapper {

    GoalInvitationEntity toEntity(GoalInvitation invitation);

    default GoalInvitation toDomain(GoalInvitationEntity entity) {
        return GoalInvitation.reconstitute(
                entity.getId(),
                entity.getGoalId(),
                entity.getEmail(),
                entity.getToken(),
                entity.getInviterUserId(),
                entity.getInvitedUserId(),
                InvitationStatus.valueOf(entity.getStatus()),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getModifiedAt(),
                entity.isActive()
        );
    }
}