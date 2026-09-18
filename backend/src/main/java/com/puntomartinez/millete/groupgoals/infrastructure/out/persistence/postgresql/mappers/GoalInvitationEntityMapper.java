package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.groupgoals.domain.model.GoalInvitation;
import com.puntomartinez.millete.groupgoals.domain.model.InvitationStatus;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalInvitationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GoalInvitationEntityMapper {

    @Mapping(
            target = "status",
            source = "status",
            qualifiedByName = "mapStatusToString"
    )
    GoalInvitationEntity toEntity(GoalInvitation domain);

    default GoalInvitation toDomain(GoalInvitationEntity entity) {
        if (entity == null) {
            return null;
        }
        return GoalInvitation.reconstitute(
                entity.getId(),
                entity.getGoalId(),
                entity.getInviterUserId(),
                entity.getInvitedUserId(),
                mapStringToStatus(entity.getStatus()),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getModifiedAt(),
                entity.isActive()
        );
    }

    @Named("mapStatusToString")
    default String mapStatusToString(InvitationStatus status) {
        return status != null ? status.name() : null;
    }

    default InvitationStatus mapStringToStatus(String status) {
        return status != null
                ? InvitationStatus.valueOf(status)
                : null;
    }
}