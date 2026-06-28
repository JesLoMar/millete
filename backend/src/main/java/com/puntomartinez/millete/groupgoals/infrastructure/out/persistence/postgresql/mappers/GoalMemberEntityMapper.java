package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.groupgoals.domain.model.GoalMember;
import com.puntomartinez.millete.groupgoals.domain.model.GoalRole;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalMemberEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GoalMemberEntityMapper {

    @Mapping(target = "role", expression = "java(mapRoleToString(domain.getRole()))")
    GoalMemberEntity toEntity(GoalMember domain);

    @Mapping(target = "role", expression = "java(mapStringToRole(entity.getRole()))")
    GoalMember toDomain(GoalMemberEntity entity);

    default String mapRoleToString(GoalRole role) {
        return role != null ? role.name() : null;
    }

    default GoalRole mapStringToRole(String role) {
        return role != null ? GoalRole.valueOf(role) : null;
    }
}