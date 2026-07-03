package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.groupgoals.domain.model.GoalInvitation;
import com.puntomartinez.millete.groupgoals.domain.model.InvitationStatus;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalInvitationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GoalInvitationEntityMapper {

    @Mapping(target = "status", expression = "java(mapStatusToString(domain.getStatus()))")
    GoalInvitationEntity toEntity(GoalInvitation domain);

    @Mapping(target = "status", expression = "java(mapStringToStatus(entity.getStatus()))")
    GoalInvitation toDomain(GoalInvitationEntity entity);

    default String mapStatusToString(InvitationStatus status) {
        return status != null ? status.name() : null;
    }

    default InvitationStatus mapStringToStatus(String status) {
        return status != null ? InvitationStatus.valueOf(status) : null;
    }
}
